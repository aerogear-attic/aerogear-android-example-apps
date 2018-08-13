package org.aerogear.android.app.memeolist.sdk;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport.Factory;

import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.configuration.ServiceConfiguration;

import javax.annotation.Nonnull;

import okhttp3.OkHttpClient;

import static org.aerogear.mobile.core.utils.SanityCheck.nonNull;

public final class SyncClient {

    public static final String TYPE = "sync";

    private static SyncClient instance;

    private final ApolloClient apolloClient;

    public SyncClient(@Nonnull OkHttpClient okHttpClient, @Nonnull String serverUrl,
                      @Nonnull String webSocketUrl) {
        ApolloClient.Builder builder = ApolloClient.builder().serverUrl(nonNull(serverUrl, "serverUrl"))
                .okHttpClient(nonNull(okHttpClient, "okHttpClient"))
                .subscriptionTransportFactory(new Factory(webSocketUrl, okHttpClient));
        apolloClient = builder.build();
    }

    public static SyncClient getInstance() {
        if (instance == null) {
            MobileCore mobileCore = MobileCore.getInstance();
            ServiceConfiguration configuration = mobileCore.getServiceConfigurationByType(TYPE);
            String serverUrl = configuration.getUrl();
            String webSocketUrl = configuration.getProperty("subscription");
            OkHttpClient okHttpClient = mobileCore.getHttpLayer().getClient();
            SyncClient.instance = new SyncClient(okHttpClient, serverUrl, webSocketUrl);
        }
        return instance;
    }

    public ApolloClient getApolloClient() {
        return apolloClient;
    }

}
