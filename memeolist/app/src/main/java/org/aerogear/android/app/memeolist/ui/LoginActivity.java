package org.aerogear.android.app.memeolist.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;

import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.graphql.CreateProfileMutation;
import org.aerogear.android.app.memeolist.graphql.ProfileQuery;
import org.aerogear.android.app.memeolist.model.UserProfile;
import org.aerogear.android.app.memeolist.util.MessageHelper;
import org.aerogear.mobile.auth.authenticator.DefaultAuthenticateOptions;
import org.aerogear.mobile.auth.user.UserPrincipal;
import org.aerogear.mobile.core.Callback;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.reactive.Responder;
import org.aerogear.mobile.sync.SyncClient;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getName();
    private static final int LOGIN_RESULT_CODE = 9831;

    @BindView(R.id.login)
    Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        if (application.isLogged()) {
            createOrRetrieveProfile();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_RESULT_CODE) {
            authService.handleAuthResult(data);
        }
    }

    @OnClick(R.id.login)
    void login() {
        mLogin.setEnabled(false);

        DefaultAuthenticateOptions options = new DefaultAuthenticateOptions(
                this, LOGIN_RESULT_CODE);

        authService.login(options, new Callback<UserPrincipal>() {
            @Override
            public void onSuccess(UserPrincipal userPrincipal) {
                createOrRetrieveProfile();
            }

            @Override
            public void onError(Throwable error) {
                MobileCore.getLogger().error(TAG, error.getMessage(), error);
                mLogin.setEnabled(true);
                new MessageHelper(getApplicationContext()).displayError(R.string.error_login);
            }
        });
    }

    public void createOrRetrieveProfile() {
        SyncClient
                .getInstance()
                .query(ProfileQuery.builder().email(authService.currentUser().getEmail()).build())
                .execute(ProfileQuery.Data.class)
                .respondWith(new Responder<Response<ProfileQuery.Data>>() {
                    @Override
                    public void onResult(Response<ProfileQuery.Data> response) {
                        if (response.hasErrors()) {
                            for (Error error : response.errors()) {
                                MobileCore.getLogger().error(error.message());
                            }
                            displayError(R.string.profile_cannot_fetch);
                        } else {
                            ProfileQuery.Data data = response.data();
                            if (data != null) {
                                List<ProfileQuery.Profile> profile = data.profile();
                                if (profile.isEmpty()) {
                                    createProfile();
                                } else {
                                    application.setUserProfile(
                                            UserProfile.from(data.profile().get(0)));
                                    navigateToMemeList();
                                }
                            }
                        }
                    }

                    @Override
                    public void onException(Exception exception) {
                        MobileCore.getLogger().error(exception.getMessage(), exception);
                        displayError(R.string.profile_cannot_fetch);
                    }
                });
    }

    private void createProfile() {
        CreateProfileMutation createProfileMutation = CreateProfileMutation.builder()
                .displayname(authService.currentUser().getName())
                .email(authService.currentUser().getEmail())
                .pictureurl("https://randomuser.me/api/portraits/lego/1.jpg")
                .build();

        SyncClient
                .getInstance()
                .mutation(createProfileMutation)
                .execute(CreateProfileMutation.Data.class)
                .respondWith(new Responder<Response<CreateProfileMutation.Data>>() {
                    @Override
                    public void onResult(Response<CreateProfileMutation.Data> response) {
                        if (response.hasErrors()) {
                            for (Error error : response.errors()) {
                                MobileCore.getLogger().error(error.message());
                            }
                            displayError(R.string.profile_create_fail);
                        } else {
                            MobileCore.getLogger().info(getString(R.string.profile_created));
                            application.setUserProfile(
                                    UserProfile.from(response.data().createProfile()));
                            navigateToMemeList();
                        }
                    }

                    @Override
                    public void onException(Exception exception) {
                        MobileCore.getLogger().error(exception.getMessage(), exception);
                        displayError(R.string.profile_create_fail);
                    }
                });
    }

    private void navigateToMemeList() {
        startActivity(new Intent(getApplicationContext(), MemeListActivity.class));
        finish();
    }

}
