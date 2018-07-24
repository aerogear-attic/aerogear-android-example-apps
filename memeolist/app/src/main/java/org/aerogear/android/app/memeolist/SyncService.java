package org.aerogear.android.app.memeolist;

import android.content.Context;
import android.support.annotation.NonNull;

import static org.aerogear.mobile.core.utils.SanityCheck.nonNull;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.ResponseField;
import com.apollographql.apollo.api.Subscription;
import com.apollographql.apollo.cache.http.ApolloHttpCache;
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore;
import com.apollographql.apollo.cache.normalized.CacheKey;
import com.apollographql.apollo.cache.normalized.CacheKeyResolver;
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy;
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper;
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.subscription.SubscriptionTransport;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport.Factory;

import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.configuration.ServiceConfiguration;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.aerogear.mobile.core.reactive.Request;
import org.aerogear.mobile.core.reactive.Requester;
import org.aerogear.mobile.core.reactive.Responder;

import java.io.File;
import java.util.Map;

import okhttp3.OkHttpClient;

public final class SyncService {

  public static final String TYPE = "sync";

  private static SyncService instance;

  private final ApolloClient apolloClient;

  public SyncService(@Nonnull OkHttpClient okHttpClient, @Nonnull String serverUrl,
                     @Nonnull String webSocketUrl, Context context) {
    DiskLruHttpCacheStore cacheStore = createHttpCache(context);

    ApolloClient.Builder builder = ApolloClient.builder();
    builder.serverUrl(nonNull(serverUrl, "serverUrl"))
            .subscriptionTransportFactory(new Factory(webSocketUrl, okHttpClient))
            .okHttpClient(nonNull(okHttpClient, "okHttpClient"));

    NormalizedCacheFactory cacheFactory = new LruNormalizedCacheFactory(EvictionPolicy.builder().maxSizeBytes(10 * 1024).build());
    //builder.normalizedCache(cacheFactory);
    //builder.httpCache(new ApolloHttpCache(cacheStore));
    createSQLCache(builder, context);

    apolloClient = builder.build();
  }

  private void createSQLCache(ApolloClient.Builder builder, Context context) {
    //Create the ApolloSqlHelper. Please note that if null is passed in as the name, you will get an in-memory SqlLite database that
    // will not persist across restarts of the app.
    ApolloSqlHelper apolloSqlHelper = ApolloSqlHelper.create(context, "memeo");

    //Create NormalizedCacheFactory
    NormalizedCacheFactory cacheFactory = new SqlNormalizedCacheFactory(apolloSqlHelper);

    //Create the cache key resolver, this example works well when all types have globally unique ids.
    CacheKeyResolver resolver = new CacheKeyResolver() {
      @NotNull
      @Override
      public CacheKey fromFieldRecordSet(@NotNull ResponseField field, @NotNull Map<String, Object> recordSet) {
        // Change to _id for server
        return formatCacheKey((String) recordSet.get("id"));
      }

      @NotNull
      @Override
      public CacheKey fromFieldArguments(@NotNull ResponseField field, @NotNull Operation.Variables variables) {
        return formatCacheKey((String) field.resolveArgument("id", variables));
      }

      private CacheKey formatCacheKey(String id) {
        if (id == null || id.isEmpty()) {
          return CacheKey.NO_KEY;
        } else {
          return CacheKey.from(id);
        }
      }
    };
    builder.normalizedCache(cacheFactory, resolver);
  }

  @NonNull
  private DiskLruHttpCacheStore createHttpCache(Context context) {
    //Directory where cached responses will be stored
    File file = new File(context.getApplicationInfo().dataDir.concat("/cache"));

    //Size in bytes of the cache
    int size = 1024 * 1024;

    //Create the http response cache store
    return new DiskLruHttpCacheStore(file, size);
  }

  public static SyncService getInstance(Context context) {
    if (instance == null) {
      MobileCore mobileCore = MobileCore.getInstance();
      ServiceConfiguration configuration = mobileCore.getServiceConfigurationByType(TYPE);
      String serverUrl = configuration.getUrl();
      String webSocketUrl = configuration.getProperty("subscription");
      OkHttpClient okHttpClient = mobileCore.getHttpLayer().getClient();
      SyncService.instance = new SyncService(okHttpClient, serverUrl, webSocketUrl, context);
    }
    return instance;
  }

  public ApolloClient getApolloClient() {
    return apolloClient;
  }

}
