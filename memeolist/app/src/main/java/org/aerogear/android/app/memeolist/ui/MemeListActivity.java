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
import org.aerogear.android.app.memeolist.SyncClient;
import org.aerogear.android.app.memeolist.controller.Login;
import org.aerogear.android.app.memeolist.graphql.AllMemesQuery;
import org.aerogear.android.app.memeolist.graphql.MemeAddedSubscription;
import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.jetbrains.annotations.NotNull;

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

    private ApolloClient apolloClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_list);
        Login login = new Login();
        login.createOrRetrieveProfile();

        ButterKnife.bind(this);
        apolloClient = SyncClient.getInstance().getApolloClient();
        mMemes.setLayoutManager(new LinearLayoutManager(this));
        new LastAdapter(memes, BR.meme)
                .map(Meme.class, R.layout.item_meme)
                .into(mMemes);

        mSwipe.setOnRefreshListener(this::retrieveMemes);

        subscribeMemes();
        retrieveMemes();
    }

    private void subscribeMemes() {
        apolloClient.subscribe(new MemeAddedSubscription()).execute(new ApolloSubscriptionCall.Callback<MemeAddedSubscription.Data>() {
            @Override
            public void onResponse(@NotNull Response<MemeAddedSubscription.Data> response) {
                MemeAddedSubscription.MemeAdded node = response.data().memeAdded();
                Meme newMeme = new Meme(node.id(), node.photourl());
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
        apolloClient
                .query(AllMemesQuery.builder().build())
                .enqueue(new ApolloCall.Callback<AllMemesQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<AllMemesQuery.Data> response) {
                        new AppExecutors().mainThread().submit(() -> {
                            memes.clear();

                            List<AllMemesQuery.AllMeme> allMemes = response.data().allMemes();

                            for (AllMemesQuery.AllMeme meme : allMemes) {
                                memes.add(new Meme(meme.id(), meme.photourl()));
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

}
