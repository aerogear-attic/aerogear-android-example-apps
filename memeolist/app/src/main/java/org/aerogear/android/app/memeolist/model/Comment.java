package org.aerogear.android.app.memeolist.model;

import org.aerogear.android.app.memeolist.graphql.CommentsQuery;
import org.aerogear.android.app.memeolist.graphql.PostCommentMutation;

import java.io.Serializable;

/**
 * Meme comment
 */
public class Comment implements Serializable {

    private final String id;
    private final String comment;
    private final UserProfile owner;

    public Comment(String id, String comment, UserProfile owner) {
        this.id = id;
        this.comment = comment;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public UserProfile getOwner() {
        return owner;
    }

    public static Comment from(CommentsQuery.Comment comment) {
        UserProfile owner = new UserProfile(
                comment.owner().id(),
                comment.owner().displayname(),
                comment.owner().email(),
                comment.owner().pictureurl()
        );

        return new Comment(comment.id(), comment.comment(), owner);
    }

    public static Comment from(PostCommentMutation.PostComment comment) {
        UserProfile owner = new UserProfile(
                comment.owner().id(),
                comment.owner().displayname(),
                comment.owner().email(),
                comment.owner().pictureurl()
        );

        return new Comment(comment.id(), comment.comment(), owner);
    }


}
