package com.rainmachine.presentation.screens.restorebackup;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Spinner;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestoreBackupDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.spinner_machine)
    Spinner spinnerMachine;

    public interface Callback {
        void onClickRestoreBackup(RestoreBackupViewModel.BackupDeviceData backupDeviceData,
                                  RestoreBackupViewModel
                                          .Backup backup);

        void onClickSkipRestoreBackup();
    }

    public static RestoreBackupDialogFragment newInstance(@NonNull RestoreBackupViewModel
            .BackupDeviceData

                                                                  backupDeviceData, String
                                                                  negativeBtnText) {
        RestoreBackupDialogFragment fragment = new RestoreBackupDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("backupDevice", Parcels.wrap(backupDeviceData));
        args.putString("negativeBtnText", negativeBtnText);
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
        builder.setTitle(R.string.restore_backup_dialog_title);

        View view = View.inflate(getContext(), R.layout.dialog_restore_backup, null);
        ButterKnife.bind(this, view);
        setupViews();
        builder.setView(view);

        builder.setPositiveButton(R.string.restore_backup_restore, (dialog, id) -> {
            RestoreBackupViewModel.BackupDeviceData backupDeviceData = Parcels.unwrap
                    (getArguments()
                            .getParcelable("backupDevice"));
            RestoreBackupViewModel.Backup backup = (RestoreBackupViewModel.Backup)
                    spinnerMachine
                            .getSelectedItem();
            callback.onClickRestoreBackup(backupDeviceData, backup);
        });
        String negativeBtnText = getArguments().getString("negativeBtnText");
        builder.setNegativeButton(negativeBtnText, (dialog, which) -> callback
                .onClickSkipRestoreBackup());
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // Do nothing special
    }

    private void setupViews() {
        RestoreBackupViewModel.BackupDeviceData backupDeviceData = Parcels.unwrap(getArguments()
                .getParcelable
                        ("backupDevice"));
        RestoreBackupSpinnerAdapter adapter = new RestoreBackupSpinnerAdapter(getContext(),
                backupDeviceData.backups, backupDeviceData);
        spinnerMachine.setAdapter(adapter);
        spinnerMachine.setSelection(0, false);
    }
}
