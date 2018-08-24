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

import com.apollographql.apollo.api.Response;
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
import org.aerogear.android.app.memeolist.util.MessageHelper;
import org.aerogear.mobile.auth.user.UserPrincipal;
import org.aerogear.mobile.core.Callback;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.aerogear.mobile.core.reactive.RequestMapFunction;
import org.aerogear.mobile.core.reactive.Requester;
import org.aerogear.mobile.core.reactive.Responder;
import org.aerogear.mobile.sync.SyncClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MemeListActivity extends BaseActivity {

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

    @OnClick(R.id.exit)
    void exit() {
        authService.logout(authService.currentUser(), new Callback<UserPrincipal>() {
            @Override
            public void onSuccess() {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }

            @Override
            public void onError(Throwable error) {
                MobileCore.getLogger().error(error.getMessage(), error);
                displayMessage(R.string.error_logout);
            }
        });
    }

    @OnClick(R.id.newMeme)
    void newMeme() {
        startActivity(new Intent(getApplicationContext(), MemeFormActivity.class));
    }

    public void createOrRetrieveProfile() {
        SyncClient
                .getInstance()
                .query(ProfileQuery.builder().email(userProfile.getEmail()).build())
                .execute(ProfileQuery.Data.class)
                .respondOn(new AppExecutors().mainThread())
                .respondWith(new Responder<Response<ProfileQuery.Data>>() {
                    @Override
                    public void onResult(Response<ProfileQuery.Data> response) {
                        ProfileQuery.Data data = response.data();
                        if (data != null) {
                            List<ProfileQuery.Profile> profile = data.profile();
                            if (profile.isEmpty()) {
                                createProfile();
                            }
                        }
                    }

                    @Override
                    public void onException(Exception exception) {
                        MobileCore.getLogger().error(exception.getMessage(), exception);
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

        SyncClient
                .getInstance()
                .mutation(createProfileMutation)
                .execute(CreateProfileMutation.Data.class)
                .respondOn(new AppExecutors().mainThread())
                .respondWith(new Responder<Response<CreateProfileMutation.Data>>() {
                    @Override
                    public void onResult(Response<CreateProfileMutation.Data> response) {
                        MobileCore.getLogger().info(getString(R.string.profile_created));
                    }

                    @Override
                    public void onException(Exception exception) {
                        MobileCore.getLogger().error(exception.getMessage(), exception);
                        displayError(R.string.profile_create_fail);
                    }
                });
    }

    private void subscribeMemes() {
        SyncClient
                .getInstance()
                .subscribe(new MemeAddedSubscription())
                .execute(MemeAddedSubscription.Data.class)
                .requestMap(response -> {
                    MemeAddedSubscription.MemeAdded node = response.data().memeAdded();
                    Meme newMeme = new Meme(node.id(), node.photourl(), new ArrayList<>());
                    return Requester.emit(newMeme);
                })
                .respondOn(new AppExecutors().mainThread())
                .respondWith(new Responder<Meme>() {
                    @Override
                    public void onResult(Meme meme) {
                        memes.add(0, meme);
                        mMemes.smoothScrollToPosition(0);
                    }

                    @Override
                    public void onException(Exception exception) {
                        MobileCore.getLogger().error(exception.getMessage(), exception);
                        displayError(R.string.error_subscribe_meme_creation);
                    }
                });
    }

    private void retrieveMemes() {
        SyncClient
                .getInstance()
                .query(new AllMemesQuery())
                .execute(AllMemesQuery.Data.class)
                .requestMap((RequestMapFunction<Response<AllMemesQuery.Data>, List<Meme>>) response -> {
                    List<Meme> memes = new ArrayList<>();

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

                    return Requester.emit(memes);
                })
                .respondOn(new AppExecutors().mainThread())
                .respondWith(new Responder<List<Meme>>() {
                    @Override
                    public void onResult(List<Meme> memeList) {
                        memes.clear();
                        memes.addAll(memeList);
                        mSwipe.setRefreshing(false);
                    }

                    @Override
                    public void onException(Exception exception) {
                        MobileCore.getLogger().error(exception.getMessage(), exception);
                        displayError(R.string.error_retrieve_memes);
                        mSwipe.setRefreshing(false);
                    }
                });
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
            SyncClient
                    .getInstance()
                    .mutation(LikeMemeMutation.builder().memeid(meme.getId()).build())
                    .execute(LikeMemeMutation.Data.class)
                    .respondOn(new AppExecutors().mainThread())
                    .respondWith(new Responder<Response<LikeMemeMutation.Data>>() {
                        @Override
                        public void onResult(Response<LikeMemeMutation.Data> value) {
                            meme.setLikes(meme.getLikes() + 1);
                            new MessageHelper(view.getContext())
                                    .displayMessage(R.string.meme_liked);
                        }

                        @Override
                        public void onException(Exception exception) {
                            MobileCore.getLogger().error(exception.getMessage(), exception);
                            new MessageHelper(view.getContext())
                                    .displayError(R.string.failed_to_like);
                        }
                    });
        }

    }

}
