package org.aerogear.android.app.memeolist.ui;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.github.nitrico.lastadapter.LastAdapter;

import org.aerogear.android.app.memeolist.BR;
import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.graphql.CommentsQuery;
import org.aerogear.android.app.memeolist.graphql.PostCommentMutation;
import org.aerogear.android.app.memeolist.model.Comment;
import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.reactive.Responder;
import org.aerogear.mobile.sync.SyncClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommentsFormActivity extends BaseActivity {

    @BindView(R.id.comment)
    TextView mComment;

    @BindView(R.id.comments)
    RecyclerView mComments;

    @BindView(R.id.post)
    TextView mPost;

    @BindView(R.id.avatar)
    ImageView mAvatar;

    private Meme meme;
    private ObservableList<Comment> comments = new ObservableArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_form);

        ButterKnife.bind(this);

        meme = (Meme) getIntent().getSerializableExtra(Meme.class.getName());

        displayAvatar(mAvatar, userProfile);

        mComments.setLayoutManager(new LinearLayoutManager(this));
        mComments.setHasFixedSize(true);

        new LastAdapter(comments, BR.comment)
                .map(Comment.class, R.layout.item_comment)
                .into(mComments);

        SyncClient
                .getInstance()
                .query(CommentsQuery.builder().memeid(meme.getId()).build())
                .execute(CommentsQuery.Data.class)
                .respondWith(new Responder<Response<CommentsQuery.Data>>() {
                    @Override
                    public void onResult(Response<CommentsQuery.Data> response) {
                        if (response.hasErrors()) {
                            for (Error error : response.errors()) {
                                MobileCore.getLogger().error(error.message());
                            }
                            displayError(R.string.error_retrieve_comments);
                        } else {
                            // FIXME: Server order is broke
                            List<CommentsQuery.Comment> c = response.data().comments();
                            List<Comment> responseComments = new ArrayList<>();
                            for (CommentsQuery.Comment comment : c) {
                                responseComments.add(Comment.from(comment));
                            }
                            Collections.sort(responseComments, (comment1, comment2) ->
                                    comment2.getId().compareTo(comment1.getId()));
                            comments.addAll(responseComments);
                        }
                    }

                    @Override
                    public void onException(Exception exception) {
                        MobileCore.getLogger().error(exception.getMessage(), exception);
                        displayError(R.string.error_retrieve_comments);
                    }
                });

    }

    @OnClick(R.id.back)
    void back() {
        finish();
    }

    @OnClick(R.id.post)
    void newComment() {
        mPost.setEnabled(false);

        PostCommentMutation postCommentMutation = PostCommentMutation.builder()
                .comment(mComment.getText().toString())
                .owner(userProfile.getId())
                .memeid(meme.getId())
                .build();

        SyncClient
                .getInstance()
                .mutation(postCommentMutation)
                .execute(PostCommentMutation.Data.class)
                .respondWith(new Responder<Response<PostCommentMutation.Data>>() {
                    @Override
                    public void onResult(Response<PostCommentMutation.Data> response) {
                        if (response.hasErrors()) {
                            for (Error error : response.errors()) {
                                MobileCore.getLogger().error(error.message());
                            }
                            displayError(R.string.comment_create_error);
                        } else {
                            mComment.setText("");
                            comments.add(0, Comment.from(response.data().postComment()));
                            mComments.smoothScrollToPosition(0);
                        }
                        mPost.setEnabled(true);
                    }

                    @Override
                    public void onException(Exception exception) {
                        MobileCore.getLogger().error(exception.getMessage(), exception);
                        displayError(R.string.comment_create_error);
                        mPost.setEnabled(true);
                    }
                });
    }

}
