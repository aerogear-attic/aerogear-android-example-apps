package org.aerogear.android.app.memeolist.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.aerogear.android.app.memeolist.R;

import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
    }

}
