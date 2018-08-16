package org.aerogear.android.app.memeolist.ui;

import android.app.Application;

import org.aerogear.android.app.memeolist.model.UserProfile;
import org.aerogear.mobile.auth.AuthService;
import org.aerogear.mobile.auth.configuration.AuthServiceConfiguration;

public class MemeolistApplication extends Application {

    private AuthService authService;
    private UserProfile userProfile;

    @Override
    public void onCreate() {
        super.onCreate();

        AuthServiceConfiguration authServiceConfig = new AuthServiceConfiguration
                .AuthConfigurationBuilder()
                .withRedirectUri("org.aerogear.android.app.memeolist://callback")
                .build();

        authService = new AuthService(authServiceConfig);

        if(authService.currentUser() != null) {
            userProfile = new UserProfile(authService.currentUser());
        }
    }

    public boolean isLogged() {
        return authService.currentUser() != null;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

}
