package org.aerogear.android.app.memeolist.controller;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;

import org.aerogear.android.app.memeolist.sdk.SyncClient;
import org.aerogear.android.app.memeolist.graphql.AllMemesQuery;
import org.aerogear.android.app.memeolist.graphql.LikeMemeMutation;
import org.aerogear.android.app.memeolist.graphql.MemeAddedSubscription;
import org.aerogear.android.app.memeolist.graphql.PostCommentMutation;
import org.aerogear.android.app.memeolist.model.Comment;

/**
 * Controller for meme operations
 */
public class MemeController {

    private final ApolloClient apolloClient;

    public MemeController() {
        apolloClient = SyncClient.getInstance().getApolloClient();
    }

    public void subscribeMemes(ApolloSubscriptionCall.Callback<MemeAddedSubscription.Data> callback) {
        apolloClient.subscribe(new MemeAddedSubscription())
                .execute(callback);

    }

    public void retrieveMemes(ApolloCall.Callback<AllMemesQuery.Data> callback) {
        apolloClient
                .query(AllMemesQuery.builder().build())
                .enqueue(callback);
    }

    public void like(String memeId, ApolloCall.Callback<LikeMemeMutation.Data> callback) {
        apolloClient
                .mutate(LikeMemeMutation.builder().memeid(memeId).build())
                .enqueue(callback);
    }

    public void addComment(Comment comment, ApolloCall.Callback<PostCommentMutation.Data> callback) {
        PostCommentMutation build = PostCommentMutation.builder().
                comment(comment.getComment()).
                owner(comment.getOwner()).
                memeid(comment.getMemeId()).build();
        apolloClient
                .mutate(build)
                .enqueue(callback);
    }
}
