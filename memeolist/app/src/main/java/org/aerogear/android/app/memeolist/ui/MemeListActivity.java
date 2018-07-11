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
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.nitrico.lastadapter.LastAdapter;

import org.aerogear.android.app.memeolist.BR;
import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.graphql.ListMemesQuery;
import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.aerogear.mobile.core.reactive.Requester;
import org.aerogear.mobile.core.reactive.Responder;
import org.aerogear.mobile.sync.SyncService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MemeListActivity extends AppCompatActivity {

    @BindView(R.id.memes)
    RecyclerView mMemes;

    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipe;

    private ObservableList<Meme> memes = new ObservableArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_list);

        ButterKnife.bind(this);

        mMemes.setLayoutManager(new LinearLayoutManager(this));
        new LastAdapter(memes, BR.meme)
                .map(Meme.class, R.layout.item_meme)
                .into(mMemes);

        mSwipe.setOnRefreshListener(() -> retrieveMemes());
    }

    @Override
    protected void onStart() {
        super.onStart();

        retrieveMemes();
    }

    private void retrieveMemes() {

        SyncService
                .getInstance()
                .query(ListMemesQuery.builder().build())
                .execute(ListMemesQuery.Data.class)
                .respondOn(new AppExecutors().mainThread())
                .requestMap(response -> {
                    List<Meme> memes = new ArrayList<>();

                    for (ListMemesQuery.AllMeme meme : response.data().allMemes()) {
                        memes.add(new Meme(meme.id(), meme.photoUrl()));
                    }

                    return Requester.emit(memes);
                })
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
