package org.aerogear.android.app.memeolist.model;

import org.aerogear.mobile.auth.user.UserPrincipal;

/**
 */
public class UserProfile {

    private String id;
    private String displayName;
    private String email;
    private String pictureUrl;
    public UserProfile(UserPrincipal loggedUser) {
        this.id = "1";
        this.displayName = loggedUser.getName();
        this.email = loggedUser.getEmail();
        this.pictureUrl = "https://randomuser.me/api/portraits/lego/1.jpg";
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

}
