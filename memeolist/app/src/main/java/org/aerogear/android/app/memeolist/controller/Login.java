package org.aerogear.android.app.memeolist.controller;

import android.util.Log;

import org.aerogear.android.app.memeolist.SyncClient;
import org.aerogear.android.app.memeolist.graphql.CreateProfileMutation;
import org.aerogear.android.app.memeolist.graphql.ProfileQuery;
import org.aerogear.android.app.memeolist.model.UserProfile;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.aerogear.mobile.core.reactive.Requester;
import org.aerogear.mobile.core.reactive.Responder;

import java.util.List;

/**
 * Temporary login controler used for creating user profile
 */
public class Login {


  public void retrieveProfile() {
    UserProfile current = UserProfile.getCurrent();
    SyncClient
            .getInstance()
            .query(ProfileQuery.builder().email(current.getEmail()).build())
            .execute(ProfileQuery.Data.class)
            .respondOn(new AppExecutors().mainThread())
            .requestMap(response -> Requester.emit(response.data().profile()))
            .respondWith(new Responder<List<ProfileQuery.Profile>>() {
              @Override
              public void onResult(List<ProfileQuery.Profile> value) {
                Log.i("LoginController", "Fetch profile" + value.toString());
              }

              @Override
              public void onException(Exception exception) {
                Log.e("LoginController", "Cannot fetch profile", exception);
              }
            });

  }


  public void createProfile() {
    UserProfile current = UserProfile.getCurrent();
    SyncClient
            .getInstance()
            .mutation(CreateProfileMutation.builder().displayname(current.getDisplayName()).email(current.getEmail()).pictureurl("").build())
            .execute(CreateProfileMutation.Data.class)
            .respondOn(new AppExecutors().mainThread())
            .requestMap(response -> Requester.emit(response.data().createProfile()))
            .respondWith(new Responder<CreateProfileMutation.CreateProfile>() {
              @Override
              public void onResult(CreateProfileMutation.CreateProfile value) {
                Log.i("LoginController", "Create profile" + value.toString());
              }

              @Override
              public void onException(Exception exception) {
                Log.e("LoginController", "Cannot fetch profile", exception);
              }
            });

  }
}
