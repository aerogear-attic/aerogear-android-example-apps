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


  public void createOrRetrieveProfile() {
    UserProfile current = UserProfile.getCurrent();
    ApolloClient apolloClient = SyncClient
            .getInstance().getApolloClient();
    ProfileQuery build = ProfileQuery.builder().email(current.getEmail()).build();
    apolloClient
            .query(build)
            .enqueue(new ApolloCall.Callback<ProfileQuery.Data>() {
              @Override
              public void onResponse(@NotNull Response<ProfileQuery.Data> response) {
                List<ProfileQuery.Profile> profile = response.data().profile();
                if (profile.isEmpty()) {
                  createProfile();
                }
                Log.i("LoginController", "Fetch profile called: " + response.data());
              }

              @Override
              public void onFailure(@NotNull ApolloException exception) {
                Log.e("LoginController", "Cannot fetch profile", exception);
              }
            });
  }


  public void createProfile() {
    UserProfile current = UserProfile.getCurrent();
    ApolloClient apolloClient = SyncClient
            .getInstance().getApolloClient();
    apolloClient
            .mutate(CreateProfileMutation.builder().displayname(current.getDisplayName()).email(current.getEmail()).pictureurl("").build())
            .enqueue(new ApolloCall.Callback<CreateProfileMutation.Data>() {
              @Override
              public void onResponse(@NotNull Response<CreateProfileMutation.Data> response) {
                Log.i("LoginController", "Created profile: " + response.data());
              }

              @Override
              public void onFailure(@NotNull ApolloException exception) {
                Log.e("LoginController", "Failed to create profile", exception);
              }
            });

  }
}
