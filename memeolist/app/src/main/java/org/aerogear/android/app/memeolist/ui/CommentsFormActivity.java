package org.aerogear.android.app.memeolist.ui;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.apollographql.apollo.api.Response;
import com.github.nitrico.lastadapter.LastAdapter;

import org.aerogear.android.app.memeolist.BR;
import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.graphql.PostCommentMutation;
import org.aerogear.android.app.memeolist.model.Comment;
import org.aerogear.android.app.memeolist.model.Meme;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.aerogear.mobile.core.reactive.Request;
import org.aerogear.mobile.core.reactive.RequestMapFunction;
import org.aerogear.mobile.core.reactive.Requester;
import org.aerogear.mobile.core.reactive.Responder;
import org.aerogear.mobile.sync.SyncClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommentsFormActivity extends BaseActivity {

    @BindView(R.id.comment_text)
    TextView commentText;

    @BindView(R.id.comments_list)
    RecyclerView commentList;

    private ObservableList<Comment> comments = new ObservableArrayList<>();
    private Meme meme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_form);

        ButterKnife.bind(this);

        commentList.setLayoutManager(new LinearLayoutManager(this));
        commentList.setHasFixedSize(true);

        new LastAdapter(comments, BR.comment)
                .map(Comment.class, R.layout.item_comment)
                .into(commentList);

        meme = (Meme) getIntent().getSerializableExtra(Meme.class.getName());
        comments.addAll(meme.getComments());
    }

    @OnClick(R.id.back)
    void back() {
        finish();
    }

    @OnClick(R.id.new_comment)
    void newComment() {
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

        SyncClient
                .getInstance()
                .mutation(postCommentMutation)
                .execute(PostCommentMutation.Data.class)
                .respondOn(new AppExecutors().mainThread())
                .requestMap(new RequestMapFunction<Response<PostCommentMutation.Data>, Comment>() {
                    @Override
                    public Request<Comment> map(Response<PostCommentMutation.Data> response) throws Exception {
                        PostCommentMutation.PostComment postComment = response.data().postComment();
                        comment.setId(postComment.id());

                        return Requester.emit(comment);
                    }
                })
                .respondWith(new Responder<Comment>() {
                    @Override
                    public void onResult(Comment value) {
                        comments.add(comment);
                    }

                    @Override
                    public void onException(Exception exception) {
                        MobileCore.getLogger().error(exception.getMessage(), exception);
                        displayError(R.string.comment_create_error);
                    }
                });
    }

}
