package com.rainmachine.presentation.screens.crop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.rainmachine.R;
import com.rainmachine.presentation.activities.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class CropActivity extends BaseActivity {

    public static final String EXTRA_IMAGE_URI = "extra_image_uri";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.img)
    CropImageView img;

    public static Intent getStartIntent(Context context, Uri imageUri) {
        Intent intent = new Intent(context, CropActivity.class);
        intent.putExtra(EXTRA_IMAGE_URI, imageUri);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.crop_title);

        img.setCropMode(CropImageView.CropMode.RATIO_4_3);
        img.setOutputWidth(640);

        Uri imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        img.startLoad(imageUri, new LoadCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.crop, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_save) {
            Uri imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
            img.startCrop(imageUri,
                    new CropCallback() {
                        @Override
                        public void onSuccess(Bitmap cropped) {
                            Timber.d("success crop");
                        }

                        @Override
                        public void onError() {
                            Timber.d("error crop");
                        }
                    }, new SaveCallback() {
                        @Override
                        public void onSuccess(Uri outputUri) {
                            Timber.d("success save");
                            Intent data = new Intent();
                            data.putExtra(EXTRA_IMAGE_URI, outputUri);
                            setResult(Activity.RESULT_OK, data);
                            finish();
                        }

                        @Override
                        public void onError() {
                            Timber.d("error save");
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}