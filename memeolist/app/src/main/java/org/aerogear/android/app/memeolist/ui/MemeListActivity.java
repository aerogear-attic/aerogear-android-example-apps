package org.aerogear.android.app.memeolist.ui;

import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.nitrico.lastadapter.LastAdapter;

import org.aerogear.android.app.memeolist.BR;
import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.controller.LoginController;
import org.aerogear.android.app.memeolist.controller.MemeController;
import org.aerogear.android.app.memeolist.graphql.AllMemesQuery;
import org.aerogear.android.app.memeolist.graphql.LikeMemeMutation;
import org.aerogear.android.app.memeolist.graphql.MemeAddedSubscription;
import org.aerogear.android.app.memeolist.model.CommentModel;
import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MemeListActivity extends AppCompatActivity {

    @BindView(R.id.memes)
    RecyclerView mMemes;

    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipe;

    private ObservableList<Meme> memes = new ObservableArrayList<>();
    private MemeController memeController;
    private LoginController loginController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_list);

        ButterKnife.bind(this);

        loginController = new LoginController();
        memeController = new MemeController();
        loginController.createOrRetrieveProfile();

        mMemes.setLayoutManager(new LinearLayoutManager(this));
        LastAdapter lastAdapter = new LastAdapter(memes, BR.meme);
        lastAdapter
                .map(Meme.class, R.layout.item_meme)
                .into(mMemes);

        mSwipe.setOnRefreshListener(this::retrieveMemes);

        subscribeMemes();
        retrieveMemes();
    }

    private void subscribeMemes() {
        memeController.subscribeMemes(new ApolloSubscriptionCall.Callback<MemeAddedSubscription.Data>() {
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
                Log.e("MemeList", "error on subscription", e);
            }

            @Override
            public void onCompleted() {
            }
        });

    }

    private void retrieveMemes() {
        memeController.retrieveMemes(new ApolloCall.Callback<AllMemesQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<AllMemesQuery.Data> response) {
                new AppExecutors().mainThread().submit(() -> {
                    memes.clear();
                    List<AllMemesQuery.AllMeme> allMemes = response.data().allMemes();
                    for (AllMemesQuery.AllMeme meme : allMemes) {
                        List<AllMemesQuery.Comment> comments = meme.comments();
                        ArrayList<CommentModel> commentsList = new ArrayList<>();
                        for (AllMemesQuery.Comment comment : comments) {
                            CommentModel commentObj = new CommentModel(comment.id(), comment.owner(), comment.comment());
                            commentObj.setMemeId(meme.id());
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

    @OnClick(R.id.newMeme)
    void newMeme() {
        startActivity(new Intent(this, MemeFormActivity.class));
    }


    public static class MemeHandler {
        public static void newComment(View view, Meme meme) {
            Intent intent = new Intent(view.getContext(), CommentsFormActivity.class);
            intent.putExtra(Meme.class.getName(), meme);
            view.getContext().startActivity(intent);
        }

        public static void like(View view, Meme meme) {
            MemeController memeController = new MemeController();
            memeController.like(meme.getId(), new ApolloCall.Callback<LikeMemeMutation.Data>() {
                @Override
                public void onResponse(@NotNull Response<LikeMemeMutation.Data> response) {
                    meme.setLikes(meme.getLikes() + 1);
                    new AppExecutors().mainThread().submit(() -> {
                        Toast.makeText(view.getContext(), R.string.meme_liked, Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    new AppExecutors().mainThread().submit(() -> {
                        Toast.makeText(view.getContext(), R.string.failed_to_like, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }

    }


}
