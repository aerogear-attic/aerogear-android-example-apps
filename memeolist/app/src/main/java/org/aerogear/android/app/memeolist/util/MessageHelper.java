package org.aerogear.android.app.memeolist.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import org.aerogear.mobile.core.executor.AppExecutors;

public final class MessageHelper {

    private final Context context;

    public MessageHelper(Context context) {
        this.context = context;
    }

    public void displayError(@StringRes int resId) {
        new AppExecutors().mainThread().submit(() -> {
            displayMessage(context.getString(resId));
        });
    }

    public void displayError(String message) {
        displayMessage(message);
    }

    public void displayMessage(@StringRes int resId) {
        displayMessage(context.getString(resId));
    }

    public void displayMessage(String message) {
        new AppExecutors().mainThread().submit(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

}
