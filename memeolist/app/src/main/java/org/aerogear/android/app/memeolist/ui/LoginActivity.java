package org.aerogear.android.app.memeolist.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import org.aerogear.android.app.memeolist.R;
import org.aerogear.mobile.auth.AuthService;
import org.aerogear.mobile.auth.authenticator.DefaultAuthenticateOptions;
import org.aerogear.mobile.auth.user.UserPrincipal;
import org.aerogear.mobile.core.Callback;
import org.aerogear.mobile.core.MobileCore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login)
    Button mLogin;

    private static final String TAG = LoginActivity.class.getName();
    private static final int LOGIN_RESULT_CODE = 9831;

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = ((MemeolistApplication) getApplication()).getAuthService();

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
            public void onSuccess(UserPrincipal models) {
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
