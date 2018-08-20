package org.aerogear.android.app.memeolist.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.model.UserProfile;
import org.aerogear.android.app.memeolist.util.MessageHelper;
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
                application.setUserProfile(new UserProfile(userPrincipal));
                startActivity(new Intent(getApplicationContext(), MemeListActivity.class));
                finish();
            }

            @Override
            public void onError(Throwable error) {
                MobileCore.getLogger().error(TAG, error.getMessage(), error);
                mLogin.setEnabled(true);
                new MessageHelper(getApplicationContext()).displayError(R.string.error_login);
            }
        });
    }

}
