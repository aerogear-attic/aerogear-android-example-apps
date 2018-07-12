package org.aerogear.android.app.memeolist.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.theartofdev.edmodo.cropper.CropImage;

import org.aerogear.android.app.memeolist.R;
import org.aerogear.android.app.memeolist.graphql.NewMemeMutation;
import org.aerogear.mobile.core.MobileCore;
import org.aerogear.mobile.core.executor.AppExecutors;
import org.aerogear.mobile.core.reactive.Requester;
import org.aerogear.mobile.core.reactive.Responder;
import org.aerogear.mobile.sync.SyncService;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MemeFormActivity extends AppCompatActivity {

    @BindView(R.id.meme)
    ImageView mMemeImage;

    @BindView(R.id.topText)
    TextView mTopText;

    @BindView(R.id.topTextPreview)
    TextView mtopTextPreview;

    @BindView(R.id.bottomText)
    TextView mBottomText;

    @BindView(R.id.bottomTextPreview)
    TextView mBottomTextPreview;

    private MaterialDialog materialDialog;

    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_form);

        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mTopText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mtopTextPreview.setText(editable.toString());
            }
        });

        mBottomText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mBottomTextPreview.setText(editable.toString());
            }
        });

        materialDialog = new MaterialDialog.Builder(this)
                .title(R.string.creating_meme)
                .content(R.string.please_wait)
                .progress(true, 0)
                .cancelable(false)
                .build();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                file = new File(result.getUri().getPath());
                Glide.with(mMemeImage).load(file).into(mMemeImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                MobileCore.getLogger().error(error.getMessage(), error);
                displayMessage(R.string.error_gettting_image);
            }
        }
    }

    @OnClick(R.id.meme)
    void choiceImage() {
        CropImage.activity()
                .setAspectRatio(1, 1)
                .setAutoZoomEnabled(false)
                .start(this);
    }

    @OnClick(R.id.send)
    void send() {
        if (isValid()) {

            materialDialog.show();

            uploadImage()
                    .respondOn(new AppExecutors().mainThread())
                    .respondWith(new Responder<String>() {
                        @Override
                        public void onResult(String imageUrl) {
                            saveMeme(imageUrl);
                        }

                        @Override
                        public void onException(Exception exception) {
                            materialDialog.dismiss();
                            MobileCore.getLogger().error(exception.getMessage(), exception);
                            displayMessage(R.string.error_upload);
                        }
                    });

        }
    }

    private void saveMeme(String imageUrl) {

        ApolloClient apolloClient = SyncService.getInstance().getApolloClient();

        NewMemeMutation mutation = NewMemeMutation.builder()
                .url(createMemeUrl(imageUrl))
                .build();

        ApolloMutationCall<NewMemeMutation.Data> mutate = apolloClient.mutate(mutation);
        mutate.enqueue(new ApolloCall.Callback<NewMemeMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<NewMemeMutation.Data> response) {
                new AppExecutors().mainThread().submit(() -> {
                    materialDialog.dismiss();
                    displayMessage(R.string.meme_created);
                    finish();
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                materialDialog.dismiss();
                MobileCore.getLogger().error(e.getMessage(), e);
                displayMessage(e.getMessage());
            }
        });

    }

    private org.aerogear.mobile.core.reactive.Request<String> uploadImage() {

        return Requester.call(() -> {

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("type", "file")
                    .addFormDataPart("image", file.getName(),
                            RequestBody.create(MediaType.parse("text/image/jpeg"), file))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .addHeader("Authorization", "Client-ID f1041ec178352c6")
                    .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder().build();
            okhttp3.Response response = client.newCall(request).execute();
            JSONObject jsonResponse = new JSONObject(response.body().string());
            return jsonResponse.getJSONObject("data").getString("link");

        }).requestOn(new AppExecutors().networkThread());

    }

    private boolean isValid() {

        if (file == null) {
            new MaterialDialog.Builder(this)
                    .title(R.string.meme_create_meme)
                    .content(R.string.meme_need_image)
                    .positiveText(R.string.ok)
                    .cancelable(false)
                    .show();
            return false;
        }

        if(mTopText.getText().toString().isEmpty()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.meme_create_meme)
                    .content(R.string.meme_need_text)
                    .positiveText(R.string.ok)
                    .cancelable(false)
                    .show();
            return false;
        }

        return true;

    }

    private void displayMessage(@StringRes int resId) {
        displayMessage(getString(resId));
    }

    private void displayMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String createMemeUrl(String imageUrl) {
        String text = mTopText.getText().toString() + "/" + mBottomText.getText().toString();

        if(mBottomText.getText().toString().isEmpty()) {
            text = mTopText.getText().toString();
        }
        
        return "https://memegen.link/custom/" + text.replace(" ", "_") + ".jpg" +
                "?alt=" + imageUrl +
                "&font=opensans-extrabold";
    }

}
