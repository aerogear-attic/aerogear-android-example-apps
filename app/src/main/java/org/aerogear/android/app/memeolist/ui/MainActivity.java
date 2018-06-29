package org.aerogear.android.app.memeolist.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.graphql.ListMemesQuery;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.sync.SyncService;

import java.util.List;

import javax.annotation.Nonnull;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getMemes(View view) {
        ApolloClient apolloClient = SyncService.getInstance().getApolloClient();

        apolloClient
                .query(ListMemesQuery.builder().build())
                .enqueue(new ApolloCall.Callback<ListMemesQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<ListMemesQuery.Data> response) {
                        List<ListMemesQuery.AllMeme> memes = response.data().allMemes();
                        MobileCore.getLogger().debug("Total memes: " + memes.size());
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        MobileCore.getLogger().error(e.getMessage(), e);
                    }
                });
    }
}
