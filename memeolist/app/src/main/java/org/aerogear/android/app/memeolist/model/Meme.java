package org.aerogear.android.app.memeolist.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.aerogear.android.app.memeolist.BR;
import org.aerogear.android.app.memeolist.graphql.AllMemesQuery;
import org.aerogear.android.app.memeolist.graphql.MemeAddedSubscription;

import java.io.Serializable;
import java.util.Objects;

public class Meme extends BaseObservable implements Serializable {

    private final String id;
    private final String photoUrl;
    private long likes;
    private final UserProfile owner;

    public Meme(String id, String photoUrl, UserProfile owner) {
        this.id = id;
        this.photoUrl = photoUrl;
        this.owner = owner;
        this.likes = 0;
    }

    public Meme(String id, String photoUrl, long likes, UserProfile owner) {
        this.id = id;
        this.photoUrl = photoUrl;
        this.owner = owner;
        this.likes = likes;
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
        notifyPropertyChanged(BR.likes);
    }

    public UserProfile getOwner() {
        return owner;
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
                UserProfile.from(meme.owner())
        );
    }

    public static Meme from(AllMemesQuery.AllMeme meme) {

        return new Meme(
                meme.id(),
                meme.photourl(),
                meme.likes(),
                UserProfile.from(meme.owner()));

    }

}
