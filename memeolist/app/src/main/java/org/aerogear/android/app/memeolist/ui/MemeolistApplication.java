package org.aerogear.android.app.memeolist.ui;

import android.app.Application;

import org.aerogear.mobile.auth.AuthService;
import org.aerogear.mobile.auth.configuration.AuthServiceConfiguration;
import org.aerogear.mobile.core.MobileCore;

public class MemeolistApplication extends Application {

    private AuthService authService;

    @Override
    public void onCreate() {
        super.onCreate();

        AuthServiceConfiguration authServiceConfig = new AuthServiceConfiguration
                .AuthConfigurationBuilder()
                .withRedirectUri("org.aerogear.android.app.memeolist://callback")
                .build();

        authService = new AuthService(authServiceConfig);
    }

    public AuthService getAuthService() {
        return authService;
    }

}
