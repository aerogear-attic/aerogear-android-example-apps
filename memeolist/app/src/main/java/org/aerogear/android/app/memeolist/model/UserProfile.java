package org.aerogear.android.app.memeolist.model;

/**
 */
public class UserProfile {

  public static UserProfile defaultUserProfile = new UserProfile("1", "Martin Martinski", "mmartin@rodhut.com");

  public UserProfile(String id, String displayName, String email) {
    this.id = id;
    this.displayName = displayName;
    this.email = email;
  }

  private String displayName;
  private String email;
  private String id;


  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public static UserProfile getCurrent() {
    return UserProfile.defaultUserProfile;
  }

  public String getId() {

    return id;
  }
}
