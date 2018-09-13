package org.aerogear.android.app.memeolist.ui;

import android.databinding.BindingAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.android.app.memeolist.model.UserProfile;
import org.aerogear.android.app.memeolist.util.MessageHelper;
import org.aerogear.mobile.auth.AuthService;
import org.jetbrains.annotations.NotNull;

public abstract class BaseActivity extends AppCompatActivity {

    protected MemeolistApplication application;
    protected AuthService authService;
    protected UserProfile userProfile;
    private MessageHelper messageHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.messageHelper = new MessageHelper(getApplicationContext());

        this.application = ((MemeolistApplication) getApplication());
        this.authService = application.getAuthService();
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

    @BindingAdapter("avatar")
    public static void displayAvatar(@NotNull ImageView imageView, @NotNull UserProfile owner) {
        Glide.with(imageView)
                .load(owner.getPictureUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

}
