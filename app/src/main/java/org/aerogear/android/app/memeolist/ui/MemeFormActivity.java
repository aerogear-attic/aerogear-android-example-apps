package org.aerogear.android.app.memeolist.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.TextView;

import org.aerogear.android.app.memeolist.R;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemeFormActivity extends AppCompatActivity {

    @BindView(R.id.topText)
    TextView mTopText;

    @BindView(R.id.topTextPreview)
    TextView mtopTextPreview;

    @BindView(R.id.bottomText)
    TextView mBottomText;

    @BindView(R.id.bottomTextPreview)
    TextView mBottomTextPreview;

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
                mtopTextPreview.setText(editable);
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
                mBottomTextPreview.setText(editable);
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
