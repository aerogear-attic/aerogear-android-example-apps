package org.aerogear.android.app.memeolist.ui;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import org.aerogear.android.app.memeolist.model.CommentModel;
import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.android.app.memeolist.model.UserProfile;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 */
public class CommentsFormActivity extends AppCompatActivity {

    private RecyclerView.LayoutManager mLayoutManager;

    private ObservableList<CommentModel> commentsData = new ObservableArrayList<>();

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
                .map(CommentModel.class, R.layout.item_comment)
                .into(commentList);
        Serializable serializableExtra = getIntent().getSerializableExtra(Meme.class.getName());
        if (serializableExtra instanceof Meme) {
            meme = (Meme) serializableExtra;
            commentsData.addAll(meme.getComments());
        } else {
            Toast.makeText(this, "Meme is missing", Toast.LENGTH_LONG).show();
        }

        // specify an adapter (see also next example)
        memeController = new MemeController();
    }

    @OnClick(R.id.new_comment)
    void newComment() {
        CommentModel commentModel = new CommentModel(UserProfile.getCurrent().getDisplayName(), commentText.getText().toString());
        commentModel.setMemeId(meme.getId());
        memeController.addComment(commentModel, new ApolloCall.Callback<PostCommentMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<PostCommentMutation.Data> response) {
                String id = response.data().postComment().id();
                Log.i(CommentsFormActivity.class.toString(), "Comment created: " + id);
                commentModel.setId(response.data().postComment().id());
                new AppExecutors().mainThread().submit(() -> {
                    commentsData.add(commentModel);
                });
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(CommentsFormActivity.class.toString(), "Error when creating comments", e);
            }
        });
    }


}
