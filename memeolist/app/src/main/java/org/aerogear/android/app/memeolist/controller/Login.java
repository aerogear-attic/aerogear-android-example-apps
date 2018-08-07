package org.aerogear.android.app.memeolist.controller;

import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.aerogear.android.app.memeolist.SyncClient;
import org.aerogear.android.app.memeolist.graphql.CreateProfileMutation;
import org.aerogear.android.app.memeolist.graphql.ProfileQuery;
import org.aerogear.android.app.memeolist.model.UserProfile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Temporary login controller used for creating user profile
 */
public class Login {

    private static final String TAG = Login.class.getName();

    private ApolloClient apolloClient;

    public Login() {
        apolloClient = SyncClient.getInstance().getApolloClient();
    }

    public void createOrRetrieveProfile() {
        UserProfile userProfile = UserProfile.getCurrent();
        ProfileQuery profileQuery = ProfileQuery.builder().email(userProfile.getEmail()).build();
        apolloClient
                .query(profileQuery)
                .enqueue(new ApolloCall.Callback<ProfileQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<ProfileQuery.Data> response) {
                        List<ProfileQuery.Profile> profile = response.data().profile();
                        if (profile.isEmpty()) {
                            createProfile();
                        }
                        Log.i(TAG, "Fetch profile called: " + response.data());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException exception) {
                        Log.e(TAG, "Cannot fetch profile", exception);
                    }
                });
    }


    public void createProfile() {
        UserProfile userProfile = UserProfile.getCurrent();
        CreateProfileMutation createProfileMutation = CreateProfileMutation.builder()
                .displayname(userProfile.getDisplayName())
                .email(userProfile.getEmail())
                .pictureurl(userProfile.getPictureUrl())
                .build();
        apolloClient
                .mutate(createProfileMutation)
                .enqueue(new ApolloCall.Callback<CreateProfileMutation.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<CreateProfileMutation.Data> response) {
                        Log.i(TAG, "Created profile: " + response.data());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException exception) {
                        Log.e(TAG, "Failed to create profile", exception);
                    }
                });
    }

}
