package org.aerogear.android.app.memeolist.model;

import org.aerogear.android.app.memeolist.graphql.AllMemesQuery;
import org.aerogear.android.app.memeolist.graphql.MemeAddedSubscription;
import org.aerogear.mobile.auth.user.UserPrincipal;

import java.io.Serializable;
import java.util.List;

/**
 */
public class UserProfile implements Serializable {

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

    public UserProfile(String id, String displayName, String email, String pictureUrl) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.pictureUrl = pictureUrl;
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

    public static UserProfile from(MemeAddedSubscription.Owner user) {
        return new UserProfile(user.id(), user.displayname(), user.email(), user.pictureurl());
    }

    public static UserProfile from(AllMemesQuery.Owner user) {
        return new UserProfile(user.id(), user.displayname(), user.email(), user.pictureurl());
    }

}
