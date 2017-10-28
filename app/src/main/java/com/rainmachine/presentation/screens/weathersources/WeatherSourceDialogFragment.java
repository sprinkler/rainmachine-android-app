package com.rainmachine.presentation.screens.weathersources;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherSourceDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.input_data_source_url)
    EditText inputDataSourceUrl;

    public interface Callback {
        void onDialogDataSourcePositiveClick(String dataSourceUrl);

        void onDialogDataSourceCancel();
    }

    public static WeatherSourceDialogFragment newInstance() {
        WeatherSourceDialogFragment fragment = new WeatherSourceDialogFragment();
        Bundle args = new Bundle();
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
        builder.setTitle(R.string.weather_sources_enter_url);

        View view = View.inflate(getContext(), R.layout.dialog_data_source_url, null);
        ButterKnife.bind(this, view);
        inputDataSourceUrl.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                save();
                dismiss();
                return true;
            }
            return false;
        });
        builder.setView(view);

        builder.setPositiveButton(R.string.all_ok, (dialog, id) -> save());
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogDataSourceCancel();
    }

    private void save() {
        String url = inputDataSourceUrl.getText().toString();
        callback.onDialogDataSourcePositiveClick(url);
    }
}
