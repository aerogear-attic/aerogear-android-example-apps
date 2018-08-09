package org.aerogear.android.app.memeolist.ui;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.github.nitrico.lastadapter.LastAdapter;

import org.aerogear.android.app.memeolist.BR;
import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.controller.MemeController;
import org.aerogear.android.app.memeolist.graphql.PostCommentMutation;
import org.aerogear.android.app.memeolist.model.Comment;
import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.android.app.memeolist.model.UserProfile;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 */
public class CommentsFormActivity extends AppCompatActivity {

    private RecyclerView.LayoutManager mLayoutManager;

    private ObservableList<Comment> commentsData = new ObservableArrayList<>();

    @BindView(R.id.comment_text)
    TextView commentText;

    @BindView(R.id.new_comment)
    Button createComment;

    @BindView(R.id.comments_list)
    RecyclerView commentList;
    private MemeController memeController;
    private Meme meme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_form);
        ButterKnife.bind(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        commentList.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        commentList.setLayoutManager(mLayoutManager);

        new LastAdapter(commentsData, BR.comment)
                .map(Comment.class, R.layout.item_comment)
                .into(commentList);
        meme = (Meme) getIntent().getSerializableExtra(Meme.class.getName());
        commentsData.addAll(meme.getComments());
        memeController = new MemeController();
    }


    @OnClick(R.id.new_comment)
    void newComment(View view) {
        Comment comment = new Comment(UserProfile.getCurrent().getDisplayName(), commentText.getText().toString());
        comment.setMemeId(meme.getId());

        memeController.addComment(comment, new ApolloCall.Callback<PostCommentMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<PostCommentMutation.Data> response) {
                String id = response.data().postComment().id();
                Log.i(CommentsFormActivity.class.toString(), "Comment created: " + id);
                comment.setId(response.data().postComment().id());
                new AppExecutors().mainThread().submit(() -> {
                    commentsData.add(comment);
                });
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(CommentsFormActivity.class.toString(), "Error when creating comments", e);
                new AppExecutors().mainThread().submit(() -> {
                    Toast.makeText(view.getContext(), R.string.comment_create_error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }



}
