package com.rainmachine.presentation.screens.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.rainmachine.R;
import com.rainmachine.domain.model.LocationInfo;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.screens.restorebackup.RestoreBackupActivity;
import com.rainmachine.presentation.screens.wizardtimezone.WizardTimezoneActivity;
import com.rainmachine.presentation.util.ExtraConstants;
import com.rainmachine.presentation.util.IntentUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class LocationActivity extends SprinklerActivity implements LocationContract.Container {

    public static final String EXTRA_SPRINKLER_ADDRESS = "extra_sprinkler_address";

    // Request code to use when launching the resolution activity
    public static final int REQ_CODE_RESOLVE_LOCATION = 1001;
    private static final int FLIPPER_MAP = 0;
    private static final int FLIPPER_NO_LOCATION_FOUND = 1;
    private static final int FLIPPER_LOCATION_PROGRESS = 2;

    @Inject
    LocationContract.Presenter presenter;

    @BindView(R.id.flipper_location)
    ViewFlipper flipperLocation;
    @BindView(R.id.btn_location)
    Button btnLocation;
    @BindView(R.id.tv_location_message)
    TextView tvLocationMessage;
    @BindView(R.id.tv_map_address)
    TextView tvMapAddress;
    @BindView(R.id.progress_text)
    TextView progressText;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    // Bool to track whether the app is already resolving an error
    private boolean resolvingLocationError = false;

    public static Intent getStartIntent(Context context, boolean isWizard, String
            sprinklerAddress) {
        Intent intent = new Intent(context, LocationActivity.class);
        intent.putExtra(ExtraConstants.IS_WIZARD, isWizard);
        intent.putExtra(EXTRA_SPRINKLER_ADDRESS, sprinklerAddress);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);

        LocationFragment fragment;
        if (savedInstanceState == null) {
            fragment = new LocationFragment();
            addFragment(R.id.map, fragment);
        } else {
            fragment = (LocationFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        }

        if (!buildGraphAndInject()) {
            return;
        }

        presenter.attachView(fragment);
        presenter.init();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(getString(R.string.location_set));
    }

    @Override
    public Object getModule() {
        return new LocationModule(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isWizard = getIntent().getBooleanExtra(ExtraConstants.IS_WIZARD, false);
        if (isWizard) {
            getMenuInflater().inflate(R.menu.skip, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_skip) {
            presenter.onClickSkip();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // Because of weird use case where the sprinkler graph is null and we force finish this
        // activity without having injected anything
        if (presenter != null) {
            presenter.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_RESOLVE_LOCATION) {
            resolvingLocationError = false;
            if (resultCode == Activity.RESULT_OK) {
                presenter.onComingBackFromResolveLocation();
            } else {
                showNoLocationFound();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.w("Location client connection failure");
        if (resolvingLocationError) {
            // Already attempting to resolve an error.
        } else if (connectionResult.hasResolution()) {
            try {
                resolvingLocationError = true;
                connectionResult.startResolutionForResult(this, REQ_CODE_RESOLVE_LOCATION);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                showNoLocationFound();
            }
        } else {
            GoogleErrorDialogFragment dialog = GoogleErrorDialogFragment.newInstance
                    (connectionResult.getErrorCode());
            showDialogSafely(dialog);
            resolvingLocationError = true;
        }
    }

    @OnClick({R.id.btn_save, R.id.btn_location, R.id.btn_manual_address,
            R.id.view_current_address, R.id.btn_edit})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_save) {
            presenter.onClickSave();
        } else if (id == R.id.btn_location) {
            Intent intent = IntentUtils.newLocationSettingsIntent();
            IntentUtils.startExternalIntentIfPossible(this, intent);
        } else if (id == R.id.btn_manual_address || id == R.id.view_current_address || id == R.id
                .btn_edit) {
            presenter.onClickShowManualAddressDialog();
        }
    }

    @Override
    public void onDialogGoogleErrorCancel() {
        resolvingLocationError = false;
        showNoLocationFound();
    }

    @Override
    public void render(LocationInfo localLocation) {
        if (!Strings.isBlank(localLocation.fullAddress)) {
            tvMapAddress.setText(localLocation.fullAddress);
        } else {
            tvMapAddress.setText(R.string.location_map_address_not_found);
        }
        showMap();
    }

    @Override
    public void showNoLocationFound() {
        tvLocationMessage.setText(R.string.location_not_found);
        btnLocation.setText(R.string.location_update_settings);
        flipperLocation.setDisplayedChild(FLIPPER_NO_LOCATION_FOUND);
    }

    @Override
    public void showNoLocationServices() {
        tvLocationMessage.setText(R.string.location_services_disabled);
        btnLocation.setText(R.string.location_enable_services);
        flipperLocation.setDisplayedChild(FLIPPER_NO_LOCATION_FOUND);
    }

    @Override
    public void showMap() {
        flipperLocation.setDisplayedChild(FLIPPER_MAP);
    }

    @Override
    public void showProgressGetLocation() {
        progressText.setText(R.string.location_getting_current);
        flipperLocation.setDisplayedChild(FLIPPER_LOCATION_PROGRESS);
    }

    @Override
    public void showProgress() {
        progressText.setText(null);
        flipperLocation.setDisplayedChild(FLIPPER_LOCATION_PROGRESS);
    }

    @Override
    public void goToTimezoneScreen() {
        startActivity(WizardTimezoneActivity.getStartIntent(this));
        finish();
    }

    @Override
    public void goToBackupsScreen() {
        startActivity(RestoreBackupActivity.getStartIntent(this, true));
        finish();
    }

    @Override
    public void closeAndGoBackToLocationScreen(String fullAddress) {
        Intent data = new Intent();
        data.putExtra(LocationActivity.EXTRA_SPRINKLER_ADDRESS, fullAddress);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    @Override
    public void showSkipDialog() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance(0,
                getString(R.string.all_are_you_sure),
                getString(R.string.location_skip_description),
                getString(R.string.all_yes), getString(R.string.all_no));
        showDialogSafely(dialog);
    }

    @Override
    public boolean isWizard() {
        return getIntent().getBooleanExtra(ExtraConstants.IS_WIZARD, false);
    }

    @Override
    public String getSprinklerAddress() {
        return getIntent().getStringExtra(LocationActivity.EXTRA_SPRINKLER_ADDRESS);
    }
}
