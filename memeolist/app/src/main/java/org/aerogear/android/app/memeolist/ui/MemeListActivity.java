package org.aerogear.android.app.memeolist.ui;

import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.nitrico.lastadapter.LastAdapter;

import org.aerogear.android.app.memeolist.BR;
import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.graphql.AllMemesQuery;
import org.aerogear.android.app.memeolist.graphql.CreateProfileMutation;
import org.aerogear.android.app.memeolist.graphql.LikeMemeMutation;
import org.aerogear.android.app.memeolist.graphql.MemeAddedSubscription;
import org.aerogear.android.app.memeolist.graphql.ProfileQuery;
import org.aerogear.android.app.memeolist.model.Comment;
import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.android.app.memeolist.sdk.SyncClient;
import org.aerogear.android.app.memeolist.util.MessageHelper;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MemeListActivity extends BaseActivity {

    private ApolloClient apolloClient;
    private ObservableList<Meme> memes = new ObservableArrayList<>();
    
    @BindView(R.id.memes)
    RecyclerView mMemes;

    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_list);

        ButterKnife.bind(this);

        if (!application.isLogged()) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        } else {

            apolloClient = SyncClient.getInstance().getApolloClient();

            mMemes.setLayoutManager(new LinearLayoutManager(this));
            mMemes.setHasFixedSize(true);

            new LastAdapter(memes, BR.meme)
                    .map(Meme.class, R.layout.item_meme)
                    .into(mMemes);

            mSwipe.setOnRefreshListener(this::retrieveMemes);

            createOrRetrieveProfile();

            subscribeMemes();
            retrieveMemes();

        }
    }

    public void createOrRetrieveProfile() {
        ProfileQuery profileQuery = ProfileQuery.builder().email(userProfile.getEmail()).build();

        apolloClient
                .query(profileQuery)
                .enqueue(new ApolloCall.Callback<ProfileQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<ProfileQuery.Data> response) {
                        List<ProfileQuery.Profile> profile = response.data().profile();
                        if (profile.isEmpty()) {
                            createProfile();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        MobileCore.getLogger().error(e.getMessage(), e);
                        displayError(R.string.profile_cannot_fetch);
                    }
                });
    }

    private void createProfile() {
        CreateProfileMutation createProfileMutation = CreateProfileMutation.builder()
                .displayname(userProfile.getDisplayName())
                .email(userProfile.getEmail())
                .pictureurl(userProfile.getPictureUrl())
                .build();

        apolloClient
                .mutate(createProfileMutation)
                .enqueue(new ApolloCall.Callback<CreateProfileMutation.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<CreateProfileMutation.Data> response) {
                        MobileCore.getLogger().info(getString(R.string.profile_created));
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        MobileCore.getLogger().error(e.getMessage(), e);
                        displayError(R.string.profile_create_fail);
                    }
                });
    }

    private void subscribeMemes() {
        apolloClient
                .subscribe(new MemeAddedSubscription())
                .execute(new ApolloSubscriptionCall.Callback<MemeAddedSubscription.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<MemeAddedSubscription.Data> response) {
                        MemeAddedSubscription.MemeAdded node = response.data().memeAdded();
                        Meme newMeme = new Meme(node.id(), node.photourl(), new ArrayList<>());
                        new AppExecutors().mainThread().submit(() -> {
                            memes.add(0, newMeme);
                            mMemes.smoothScrollToPosition(0);
                        });
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        MobileCore.getLogger().error(e.getMessage(), e);

                    }

                    @Override
                    public void onCompleted() {
                    }
                });
    }

    private void retrieveMemes() {
        apolloClient
                .query(new AllMemesQuery())
                .enqueue(new ApolloCall.Callback<AllMemesQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<AllMemesQuery.Data> response) {
                        new AppExecutors().mainThread().submit(() -> {
                            memes.clear();

                            List<AllMemesQuery.AllMeme> allMemes = response.data().allMemes();
                            for (AllMemesQuery.AllMeme meme : allMemes) {
                                List<AllMemesQuery.Comment> comments = meme.comments();
                                ArrayList<Comment> commentsList = new ArrayList<>();
                                for (AllMemesQuery.Comment comment : comments) {
                                    Comment commentObj = new Comment(
                                            comment.id(),
                                            comment.owner(),
                                            comment.comment(),
                                            meme.id()
                                    );
                                    commentsList.add(commentObj);
                                }
                                Meme currentMeme = new Meme(meme.id(), meme.photourl(), commentsList);
                                currentMeme.setLikes(meme.likes());
                                currentMeme.setOwner(meme.owner());
                                memes.add(currentMeme);
                            }

                            mSwipe.setRefreshing(false);
                        });
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        MobileCore.getLogger().error(e.getMessage(), e);
                        mSwipe.setRefreshing(false);
                    }
                });
    }

    @OnClick(R.id.newMeme)
    void newMeme() {
        startActivity(new Intent(this, MemeFormActivity.class));
    }

    @BindingAdapter("memeImage")
    public static void displayMeme(ImageView imageView, Meme meme) {
        CircularProgressDrawable placeHolder = new CircularProgressDrawable(imageView.getContext());
        placeHolder.setStrokeWidth(5f);
        placeHolder.setCenterRadius(30f);
        placeHolder.start();

        Glide.with(imageView)
                .load(meme.getPhotoUrl())
                .apply(RequestOptions.placeholderOf(placeHolder))
                .into(imageView);
    }

    public static class MemeHandler {
        public static void newComment(View view, Meme meme) {
            Intent intent = new Intent(view.getContext(), CommentsFormActivity.class);
            intent.putExtra(Meme.class.getName(), meme);
            view.getContext().startActivity(intent);
        }

        public static void like(View view, Meme meme) {
            SyncClient.getInstance()
                    .getApolloClient()
                    .mutate(LikeMemeMutation.builder().memeid(meme.getId()).build())
                    .enqueue(new ApolloCall.Callback<LikeMemeMutation.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<LikeMemeMutation.Data> response) {
                            meme.setLikes(meme.getLikes() + 1);
                            new AppExecutors().mainThread().submit(() -> {
                                new MessageHelper(view.getContext())
                                        .displayMessage(R.string.meme_liked);
                            });
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                            new AppExecutors().mainThread().submit(() -> {
                                new MessageHelper(view.getContext())
                                        .displayError(R.string.failed_to_like);
                            });
                        }
                    });
        }

    }

}
