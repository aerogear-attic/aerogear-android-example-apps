package org.aerogear.android.app.memeolist.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.aerogear.android.app.memeolist.graphql.AllMemesQuery;
import org.aerogear.android.app.memeolist.graphql.MemeAddedSubscription;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Meme extends BaseObservable implements Serializable {

    private final String id;
    private final String photoUrl;
    private long likes;
    private final UserProfile owner;
    private final List<Comment> comments;

    public Meme(String id, String photoUrl, UserProfile owner) {
        this.id = id;
        this.photoUrl = photoUrl;
        this.owner = owner;
        this.likes = 0;
        this.comments = new ArrayList<>();
    }

    public Meme(String id, String photoUrl, long likes, UserProfile owner, List<Comment> comments) {
        this.id = id;
        this.photoUrl = photoUrl;
        this.owner = owner;
        this.likes = likes;
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    @Bindable
    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public UserProfile getOwner() {
        return owner;
    }

    public List<Comment> getComments() {
        return comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meme meme = (Meme) o;
        return Objects.equals(id, meme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Meme from(MemeAddedSubscription.MemeAdded meme) {
        return new Meme(
                meme.id(),
                meme.photourl(),
                UserProfile.from(meme.owner().get(0))
        );
    }

    public static Meme from(AllMemesQuery.AllMeme meme) {

        ArrayList<Comment> comments = new ArrayList<>();
        for (AllMemesQuery.Comment comment : meme.comments()) {
            comments.add(Comment.from(comment, meme.id()));
        }

        return new Meme(
                meme.id(),
                meme.photourl(),
                meme.likes(),
                UserProfile.from(meme.owner().get(0)),
                comments);

    }

}
