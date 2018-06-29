package org.aerogear.android.app.memeolist.ui;

import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.github.nitrico.lastadapter.LastAdapter;

import org.aerogear.android.app.memeolist.BR;
import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.graphql.ListMemesQuery;
import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.aerogear.mobile.sync.SyncService;

import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.memes)
    RecyclerView mMemes;

    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipe;

    private ObservableList<ListMemesQuery.AllMeme> memes = new ObservableArrayList<>();
    private ApolloClient apolloClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        apolloClient = SyncService.getInstance().getApolloClient();

        mMemes.setLayoutManager(new LinearLayoutManager(this));
        new LastAdapter(memes, BR.meme)
                .map(ListMemesQuery.AllMeme.class, R.layout.item_meme)
                .into(mMemes);

        mSwipe.setOnRefreshListener(() -> retrieveMemes());
    }

    @Override
    protected void onStart() {
        super.onStart();

        retrieveMemes();
    }

    private void retrieveMemes() {
        apolloClient
                .query(ListMemesQuery.builder().build())
                .enqueue(new ApolloCall.Callback<ListMemesQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<ListMemesQuery.Data> response) {
                        new AppExecutors().mainThread().submit(() -> {
                            memes.clear();
                            memes.addAll(response.data().allMemes());

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
    public static void displayMeme(ImageView imageView, ListMemesQuery.AllMeme meme) {
        Glide.with(imageView).load(meme.photoUrl()).into(imageView);
    }

}
