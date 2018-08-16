package org.aerogear.android.app.memeolist.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.model.UserProfile;
import org.aerogear.mobile.auth.AuthService;
import org.aerogear.mobile.auth.authenticator.DefaultAuthenticateOptions;
import org.aerogear.mobile.auth.user.UserPrincipal;
import org.aerogear.mobile.core.Callback;
import org.aerogear.mobile.core.MobileCore;

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_RESULT_CODE) {
            application.getAuthService().handleAuthResult(data);
        }
    }

    @OnClick(R.id.login)
    void login() {
        mLogin.setEnabled(false);

        DefaultAuthenticateOptions options = new DefaultAuthenticateOptions(
                this, LOGIN_RESULT_CODE);
        application.getAuthService().login(options, new Callback<UserPrincipal>() {
            @Override
            public void onSuccess(UserPrincipal userPrincipal) {
                MobileCore.getInstance().getHttpInterceptorLayer().add((builder) -> {
                    AuthService authService = application.getAuthService();
                    UserPrincipal user = authService.currentUser();
                    if (user != null && user.getAccessToken() != null) {
                        String accessToken = user.getAccessToken();
                        builder.addHeader("Authorization", "Bearer " + accessToken).build();
                    } else {
                        // authService.refreshToken()??
                    }
                    return builder;
                });
                application.setUserProfile(new UserProfile(userPrincipal));
                startActivity(new Intent(getApplicationContext(), MemeListActivity.class));
            }

            @Override
            public void onError(Throwable error) {
                MobileCore.getLogger().error(TAG, error.getMessage(), error);
                mLogin.setEnabled(true);
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }

}
