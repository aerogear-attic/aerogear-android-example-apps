package org.aerogear.android.app.memeolist.sdk;

import android.content.Context;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.ResponseField;
import com.apollographql.apollo.cache.normalized.CacheKey;
import com.apollographql.apollo.cache.normalized.CacheKeyResolver;
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper;
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Wrapper class that provides query storage/cache for grapqhl queries.
 * Data is being saved into SQL database.
 */
public class QueryStoreWrapper {

    private Context context;

    public QueryStoreWrapper(Context context) {
        this.context = context;
    }


    /**
     * Create query cache
     *
     * @param builder  builder for apollo client
     * @param dbName   name for database file
     * @param cacheKey key that will be used for caching
     *                 Always return this id for caching data objects.
     */
    public void create(ApolloClient.Builder builder, String dbName, String cacheKey) {
        // Create the ApolloSqlHelper. Please note that if null is passed in as the name, you will get an in-memory SqlLite database that
        // will not persist across restarts of the app.
        ApolloSqlHelper apolloSqlHelper = ApolloSqlHelper.create(context, dbName);

        //Create NormalizedCacheFactory
        NormalizedCacheFactory cacheFactory = new SqlNormalizedCacheFactory(apolloSqlHelper);

        //Create the cache key resolver, this example works well when all types have globally unique ids.
        CacheKeyResolver resolver = new CacheKeyResolver() {
            @NotNull
            @Override
            public CacheKey fromFieldRecordSet(@NotNull ResponseField field, @NotNull Map<String, Object> recordSet) {
                // Change to _id for server
                return formatCacheKey((String) recordSet.get(cacheKey));
            }

            @NotNull
            @Override
            public CacheKey fromFieldArguments(@NotNull ResponseField field, @NotNull Operation.Variables variables) {
                return formatCacheKey((String) field.resolveArgument(cacheKey, variables));
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
}
