package com.rainmachine.presentation.screens.zonedetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.crop.CropActivity;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class ZoneDetailsActivity extends SprinklerActivity {

    public static final String EXTRA_RESULT_ZONE = "extra_result_zone";

    private static final int FLIPPER_CHILD_MAIN = 0;
    private static final int FLIPPER_CHILD_ADVANCED = 1;
    private static final int FLIPPER_CHILD_WEATHER = 2;
    private static final int FLIPPER_CHILD_PROGRESS = 3;
    private static final int FLIPPER_CHILD_ERROR = 4;

    private static final int REQ_CODE_CROP = 1111;

    public static final String EXTRA_ZONE_ID = "extra_zone_id";

    @Inject
    ZoneDetailsPresenter presenter;
    @Inject
    ZoneDetailsMainPresenter mainPresenter;
    @Inject
    ZoneDetailsWeatherPresenter weatherPresenter;
    @Inject
    ZoneDetailsAdvancedPresenter advancedPresenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.flipper)
    ViewFlipper flipper;
    @BindView(R.id.view_main)
    ZoneDetailsMainView mainView;
    @BindView(R.id.view_advanced)
    ZoneDetailsAdvancedView advancedView;

    private View customActionBarView;
    private boolean showDefaultsMenuItem;

    public static Intent getStartIntent(Context context, long zoneId) {
        Intent intent = new Intent(context, ZoneDetailsActivity.class);
        intent.putExtra(EXTRA_ZONE_ID, zoneId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_zone_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);

        buildCustomActionBar();
        showMainView();

        presenter.attachView(this);
        if (getLastCustomNonConfigurationInstance() != null) {
            presenter.setRetainedState(getLastCustomNonConfigurationInstance());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather, menu);
        MenuItem defaultsMenuItem = menu.findItem(R.id.menu_defaults);
        defaultsMenuItem.setVisible(showDefaultsMenuItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_defaults) {
            advancedPresenter.onClickDefaultsAdvanced();
            return true;
        } else if (id == android.R.id.home) {
            doBackLogic();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra(CropActivity.EXTRA_IMAGE_URI);
                if (uri != null) {
                    mainPresenter.onComingBackFromCrop(uri);
                }
            }
        } else {
            EasyImage.handleActivityResult(requestCode, resultCode, data, this, new
                    DefaultCallback() {

                        @Override
                        public void onImagePickerError(Exception e, EasyImage.ImageSource source,
                                                       int type) {
                            // Some error handling
                        }

                        @Override
                        public void onImagePicked(File imageFile, EasyImage.ImageSource source,
                                                  int type) {
                            mainPresenter.onComingBackFromPickingImage(imageFile);
                        }

                        @Override
                        public void onCanceled(EasyImage.ImageSource source, int type) {
                            // Cancel handling, you might wanna remove taken photo if it was
                            // canceled
                            if (source == EasyImage.ImageSource.CAMERA) {
                                File photoFile = EasyImage.lastlyTakenButCanceledPhoto
                                        (ZoneDetailsActivity.this);
                                if (photoFile != null) {
                                    photoFile.delete();
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        doBackLogic();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return presenter.getRetainedState();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.destroy();
        }
        super.onDestroy();
    }

    public Object getModule() {
        return new ZoneDetailsModule(this);
    }

    private void doBackLogic() {
        if (flipper.getDisplayedChild() == FLIPPER_CHILD_ADVANCED) {
            advancedView.doCustomBackLogic();
        } else if (flipper.getDisplayedChild() == FLIPPER_CHILD_WEATHER) {
            showMainView();
        } else {
            presenter.onClickLeaveScreen();
        }
    }

    public void hideDefaultsMenuItem() {
        showDefaultsMenuItem = false;
        supportInvalidateOptionsMenu();
    }

    private void showDefaultsMenuItem() {
        showDefaultsMenuItem = true;
        supportInvalidateOptionsMenu();
    }

    public void toggleCustomActionBar(boolean makeVisible) {
        customActionBarView.setVisibility(makeVisible ? View.VISIBLE : View.INVISIBLE);
    }

    public void showMainView() {
        flipper.setDisplayedChild(FLIPPER_CHILD_MAIN);
        hideDefaultsMenuItem();

        // Show the custom action bar view and hide the normal Home icon and title.
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(customActionBarView, new ActionBar.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void showAdvancedView() {
        flipper.setDisplayedChild(FLIPPER_CHILD_ADVANCED);
        showDefaultsMenuItem();

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar
                .DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setCustomView(null);
        getSupportActionBar().setSubtitle(null);
    }

    public void showWeatherView() {
        flipper.setDisplayedChild(FLIPPER_CHILD_WEATHER);
        hideDefaultsMenuItem();

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar
                .DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setCustomView(null);
        getSupportActionBar().setSubtitle(null);
    }

    public void showProgress() {
        flipper.setDisplayedChild(FLIPPER_CHILD_PROGRESS);
    }

    public void showError() {
        flipper.setDisplayedChild(FLIPPER_CHILD_ERROR);
    }

    private void buildCustomActionBar() {
        customActionBarView = View.inflate(getSupportActionBar().getThemedContext(), R.layout
                .include_actionbar_discard_save, null);
        customActionBarView.findViewById(R.id.actionbar_save).setOnClickListener(
                v -> presenter.onClickSave());
        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                v -> presenter.onClickLeaveScreen());
    }

    public void updateViewModel(ZoneDetailsViewModel viewModel) {
        mainPresenter.updateViewModel(viewModel);
        weatherPresenter.updateViewModel(viewModel);
        advancedPresenter.updateViewModel(viewModel);
    }

    public void showCropScreen(Uri imageUri) {
        startActivityForResult(CropActivity.getStartIntent(this, imageUri), REQ_CODE_CROP);
    }
}
