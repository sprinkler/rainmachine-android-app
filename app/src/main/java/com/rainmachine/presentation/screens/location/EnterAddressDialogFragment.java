package com.rainmachine.presentation.screens.location;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.rainmachine.R;
import com.rainmachine.domain.model.Autocomplete;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.GenericErrorDealer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class EnterAddressDialogFragment extends DialogFragment implements AdapterView
        .OnItemClickListener {

    private static final int THRESHOLD = 10; // characters

    @Inject
    LocationContract.Presenter presenter;

    @BindView(R.id.auto_complete)
    AutoCompleteTextView autoComplete;

    private AddressSuggestionAdapter adapter;
    private Autocomplete selectedLocation;
    private boolean dialogReady;
    private CompositeDisposable disposables;

    public EnterAddressDialogFragment() {
        disposables = new CompositeDisposable();
    }

    public static EnterAddressDialogFragment newInstance(String previousAddress) {
        EnterAddressDialogFragment fragment = new EnterAddressDialogFragment();
        Bundle args = new Bundle();
        args.putString("previousAddress", previousAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((SprinklerActivity) getActivity()).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.location_enter_address);

        View view = View.inflate(getContext(), R.layout.dialog_enter_address, null);
        ButterKnife.bind(this, view);
        setupViews();
        builder.setView(view);

        builder.setPositiveButton(R.string.all_ok, (dialog, id) -> {
            // This is replaced in onShowListener
        });
        Dialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            if (!dialogReady) {
                Button button = ((AlertDialog) dialog1).getButton(DialogInterface
                        .BUTTON_POSITIVE);
                button.setOnClickListener(v -> onPositiveButton());
                dialogReady = true;
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // Do nothing special
    }

    @Override
    public void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedLocation = adapter.getItem(position);
    }

    private void onPositiveButton() {
        if (selectedLocation == null) {
            autoComplete.setError(getString(R.string.location_invalid));
        } else {
            presenter.onDialogEnterAddressPositiveClick(selectedLocation);
            dismissAllowingStateLoss();
        }
    }

    private void setupViews() {
        adapter = new AddressSuggestionAdapter(getActivity(), new ArrayList<>());
        autoComplete.setAdapter(adapter);
        autoComplete.setThreshold(THRESHOLD);
        disposables.add(RxTextView
                .textChanges(autoComplete)
                .doOnNext(charSequence -> autoComplete.setError(null))
                .debounce(150, TimeUnit.MILLISECONDS)
                .filter(charSequence -> presenter != null && charSequence.length() >= THRESHOLD)
                .switchMap(s -> presenter
                        .textChanges(s)
                        .onErrorResumeNext(Observable.empty()))
                .doOnError(GenericErrorDealer.INSTANCE)
                .subscribeWith(new AutocompleteSuggestionsSubscriber()));
        autoComplete.setOnItemClickListener(this);
        autoComplete.setText(getArguments().getString("previousAddress"));
        autoComplete.setSelection(autoComplete.length());
    }

    public void updateContent(List<Autocomplete> data) {
        adapter.setItems(data);
    }

    private final class AutocompleteSuggestionsSubscriber extends
            DisposableObserver<List<Autocomplete>> {

        @Override
        public void onNext(List<Autocomplete> data) {
            updateContent(data);
        }

        @Override
        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
            // Do nothing
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
