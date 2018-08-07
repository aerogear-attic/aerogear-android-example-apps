package org.aerogear.android.app.memeolist.model;

/**
 */
public class UserProfile {

    public static UserProfile defaultUserProfile = new UserProfile(
            "1",
            "Martin Martinski",
            "mmartin@rodhut.com",
            "https://randomuser.me/api/portraits/lego/1.jpg");

    public UserProfile(String id, String displayName, String email, String pictureUrl) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.pictureUrl = pictureUrl;
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

    public static UserProfile getCurrent() {
        return UserProfile.defaultUserProfile;
    }

}
