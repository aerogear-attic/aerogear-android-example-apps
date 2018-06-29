package org.aerogear.mobile.sync;

import com.apollographql.apollo.ApolloClient;

import okhttp3.OkHttpClient;

public class SyncService {

    private static SyncService instance;

    private final ApolloClient apolloClient;

    public SyncService(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        apolloClient = ApolloClient.builder()
                .serverUrl(url)
                .okHttpClient(okHttpClient)
                .build();
    }

    public static SyncService getInstance() {
        if (instance == null) {
            // TODO replace for the URL from mobile-services.json
            String url = "https://api.graph.cool/simple/v1/cjiyvc1wa40kg011846ev0ff8";
            instance = new SyncService(url);
        }
        return instance;
    }

    public ApolloClient getApolloClient() {
        return apolloClient;
    }

}
