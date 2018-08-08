package org.aerogear.android.app.memeolist.model;

import java.io.Serializable;

/**
 * Meme comment
 */
public class Comment implements Serializable {

    private String id;
    private String comment;
    private String owner;
    private String memeId;

    public Comment(String owner, String comment) {
        this.comment = comment;
        this.owner = owner;
    }

    public Comment(String id, String comment, String owner, String memeId) {
        this.id = id;
        this.comment = comment;
        this.owner = owner;
        this.memeId = memeId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMemeId() {
        return memeId;
    }

    public void setMemeId(String memeId) {
        this.memeId = memeId;
    }
}
