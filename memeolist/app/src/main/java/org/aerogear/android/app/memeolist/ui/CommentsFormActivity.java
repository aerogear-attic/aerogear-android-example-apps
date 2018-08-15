package org.aerogear.android.app.memeolist.ui;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.github.nitrico.lastadapter.LastAdapter;

import org.aerogear.android.app.memeolist.BR;
import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.graphql.PostCommentMutation;
import org.aerogear.android.app.memeolist.model.Comment;
import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.android.app.memeolist.sdk.SyncClient;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommentsFormActivity extends BaseActivity {

    @BindView(R.id.comment_text)
    TextView commentText;

    @BindView(R.id.comments_list)
    RecyclerView commentList;

    private ObservableList<Comment> comments = new ObservableArrayList<>();
    private ApolloClient apolloClient;
    private Meme meme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_form);

        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        apolloClient = SyncClient.getInstance().getApolloClient();

        commentList.setLayoutManager(new LinearLayoutManager(this));
        commentList.setHasFixedSize(true);

        new LastAdapter(comments, BR.comment)
                .map(Comment.class, R.layout.item_comment)
                .into(commentList);

        meme = (Meme) getIntent().getSerializableExtra(Meme.class.getName());
        comments.addAll(meme.getComments());
    }

    @OnClick(R.id.new_comment)
    void newComment(View view) {
        Comment comment = new Comment(
                userProfile.getDisplayName(),
                commentText.getText().toString(),
                meme.getId()
        );

        PostCommentMutation postCommentMutation = PostCommentMutation.builder().
                comment(comment.getComment()).
                owner(comment.getOwner()).
                memeid(comment.getMemeId())
                .build();

        apolloClient.mutate(postCommentMutation)
                .enqueue(new ApolloCall.Callback<PostCommentMutation.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<PostCommentMutation.Data> response) {
                        PostCommentMutation.PostComment postComment = response.data().postComment();

                        MobileCore.getLogger().info("Comment created: " + postComment.id());

                        comment.setId(postComment.id());

                        new AppExecutors().mainThread().submit(() -> {
                            comments.add(comment);
                        });
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        MobileCore.getLogger().error(e.getMessage(), e);
                        new AppExecutors().mainThread().submit(() ->
                                displayError(R.string.comment_create_error));
                    }
                });
    }

}
