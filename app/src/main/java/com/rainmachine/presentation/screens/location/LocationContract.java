package com.rainmachine.presentation.screens.location;

import android.support.v4.app.DialogFragment;

import com.google.android.gms.common.api.GoogleApiClient;
import com.rainmachine.domain.model.Autocomplete;
import com.rainmachine.domain.model.LocationInfo;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;

import java.util.List;

import io.reactivex.Observable;

interface LocationContract {

    interface View {

        void render(LocationInfo location);
    }

    interface Container extends GoogleErrorDialogFragment.Callback, GoogleApiClient
            .OnConnectionFailedListener {

        boolean isWizard();

        String getSprinklerAddress();

        void render(LocationInfo localLocation);

        void showNoLocationServices();

        void showProgressGetLocation();

        void showNoLocationFound();

        void showDialogSafely(DialogFragment dialog);

        void showProgress();

        void showMap();

        void goToTimezoneScreen();

        void closeAndGoBackToLocationScreen(String fullAddress);

        void goToBackupsScreen();

        void showSkipDialog();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            ActionMessageDialogFragment.Callback {
        void start();

        void stop();

        void onClickSkip();

        void onComingBackFromResolveLocation();

        void onClickSave();

        void onClickShowManualAddressDialog();

        void onDialogEnterAddressPositiveClick(Autocomplete selectedLocation);

        Observable<List<Autocomplete>> textChanges(CharSequence s);
    }
}
