package org.aerogear.android.app.memeolist.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;

import org.aerogear.android.app.memeolist.model.UserProfile;
import org.aerogear.android.app.memeolist.util.MessageHelper;

public abstract class BaseActivity extends AppCompatActivity {

    protected MemeolistApplication application;
    protected UserProfile userProfile;
    private MessageHelper messageHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.messageHelper = new MessageHelper(getApplicationContext());

        this.application = ((MemeolistApplication) getApplication());
        this.userProfile = application.getUserProfile();
    }

    public void displayError(@StringRes int resId) {
        messageHelper.displayError(resId);
    }

    public void displayError(String message) {
        messageHelper.displayError(message);
    }

    public void displayMessage(@StringRes int resId) {
        messageHelper.displayMessage(resId);
    }

    public void displayMessage(String message) {
        messageHelper.displayMessage(message);
    }

}
