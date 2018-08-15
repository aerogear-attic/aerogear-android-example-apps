package org.aerogear.android.app.memeolist.model;

import org.aerogear.mobile.auth.user.UserPrincipal;

/**
 */
public class UserProfile {

    public UserProfile(UserPrincipal loggedUser) {
        this.id = loggedUser.getUsername();
        this.displayName = loggedUser.getName();
        this.email = loggedUser.getEmail();
        this.pictureUrl = "https://randomuser.me/api/portraits/lego/1.jpg";
    }

    private String id;
    private String displayName;
    private String email;
    private String pictureUrl;

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
