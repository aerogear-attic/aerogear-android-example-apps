package org.aerogear.mobile.sync;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport.Factory;

import okhttp3.OkHttpClient;

public class SyncService {

    private static SyncService instance;

    private final ApolloClient apolloClient;

    public SyncService(String serverUrl) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        apolloClient = ApolloClient.builder()
                .serverUrl(serverUrl)
                .okHttpClient(okHttpClient)
                .build();
    }

    public SyncService(String serverUrl, String webSocketUrl) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        apolloClient = ApolloClient.builder()
                .serverUrl(serverUrl)
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new Factory(webSocketUrl, okHttpClient))
                .build();
    }

    public static SyncService getInstance() {
        if (instance == null) {
            // TODO replace for the URL from mobile-services.json
            String serverUrl = "https://api.graph.cool/simple/v1/cjiyvc1wa40kg011846ev0ff8";
            String webSocketUrl = "wss://subscriptions.us-west-2.graph.cool/v1/cjiyvc1wa40kg011846ev0ff8";
            instance = new SyncService(serverUrl, webSocketUrl);
        }
        return instance;
    }

    public ApolloClient getApolloClient() {
        return apolloClient;
    }

}
