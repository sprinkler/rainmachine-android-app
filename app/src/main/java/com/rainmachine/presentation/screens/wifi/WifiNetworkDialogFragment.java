package com.rainmachine.presentation.screens.wifi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.WifiSettings;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Toasts;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WifiNetworkDialogFragment extends DialogFragment implements AdapterView
        .OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    @Inject
    WifiContract.Presenter presenter;

    @BindView(R.id.scroll)
    ScrollView scrollView;
    @BindView(R.id.input_ssid)
    EditText inputSSID;
    @BindView(R.id.spinner_security)
    Spinner spinnerSecurity;
    @BindView(R.id.tv_password)
    TextView tvPassword;
    @BindView(R.id.input_password)
    EditText inputPassword;
    @BindView(R.id.spinner_ip_settings)
    Spinner spinnerIPSettings;
    @BindView(R.id.tv_ip_settings)
    TextView tvIpSettings;
    @BindView(R.id.check_advanced_options)
    CheckBox checkAdvancedOptions;
    @BindView(R.id.check_show_password)
    CheckBox checkShowPassword;
    @BindView(R.id.view_address_info)
    ViewGroup viewAddressInfo;
    @BindView(R.id.input_ip_address)
    EditText inputIPAddress;
    @BindView(R.id.input_netmask)
    EditText inputNetmask;
    @BindView(R.id.input_gateway)
    EditText inputGateway;
    @BindView(R.id.input_dns)
    EditText inputDns;

    private boolean dialogReady;

    public static WifiNetworkDialogFragment newInstance(boolean isWizard) {
        WifiNetworkDialogFragment fragment = new WifiNetworkDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("isWizard", isWizard);
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
        builder.setTitle(R.string.wifi_add_network);

        View view = View.inflate(getContext(), R.layout.dialog_add_network, null);
        ButterKnife.bind(this, view);

        ArrayAdapter<CharSequence> adapterSecurity = ArrayAdapter.createFromResource(getActivity(),
                R.array.wifi_security, android.R.layout.simple_spinner_item);
        adapterSecurity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSecurity.setAdapter(adapterSecurity);
        spinnerSecurity.setSelection(0, false);
        spinnerSecurity.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapterSettings = ArrayAdapter.createFromResource(getActivity(),
                R.array.wifi_ip_settings, android.R.layout.simple_spinner_item);
        adapterSettings.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIPSettings.setAdapter(adapterSettings);
        spinnerIPSettings.setSelection(0, false);
        spinnerIPSettings.setOnItemSelectedListener(this);

        checkAdvancedOptions.setOnCheckedChangeListener(this);
        checkShowPassword.setOnCheckedChangeListener(this);
        builder.setView(view);

        builder.setPositiveButton(R.string.wifi_connect, (dialog, id) -> {
            // This is replaced in onShowListener
        });
        builder.setNegativeButton(R.string.all_cancel, (dialog, which) -> {
            // Do nothing
            dialog.cancel();
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int spinnerId = parent.getId();
        if (spinnerId == R.id.spinner_security) {
            tvPassword.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
            inputPassword.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
            checkShowPassword.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
            scrollToBottom();
        } else if (spinnerId == R.id.spinner_ip_settings) {
            showHideAddressInfo();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.check_advanced_options) {
            if (isChecked) {
                tvIpSettings.setVisibility(View.VISIBLE);
                spinnerIPSettings.setVisibility(View.VISIBLE);
                showHideAddressInfo();
            } else {
                tvIpSettings.setVisibility(View.GONE);
                spinnerIPSettings.setVisibility(View.GONE);
                viewAddressInfo.setVisibility(View.GONE);
                scrollToBottom();
            }
        } else if (id == R.id.check_show_password) {
            inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | (isChecked ? InputType
                    .TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType
                    .TYPE_TEXT_VARIATION_PASSWORD));
            inputPassword.setSelection(inputPassword.getText().length());
        }
    }

    private void onPositiveButton() {
        boolean isSuccess = true;
        int positionSecurity = spinnerSecurity.getSelectedItemPosition();

        String ssid = inputSSID.getText().toString();
        if (Strings.isBlank(ssid)) {
            isSuccess = false;
            inputSSID.setError(getString(R.string.all_error_required));
        }

        String password = "";
        if (positionSecurity > 0) {
            password = inputPassword.getText().toString();
            if (Strings.isBlank(password)) {
                isSuccess = false;
                inputPassword.setError(getString(R.string.all_error_required));
            }
        }

        int networkType = WifiSettings.NETWORK_TYPE_DHCP;
        String ipAddress = null;
        String netmask = null;
        String gateway = null;
        String dns = null;
        if (checkAdvancedOptions.isChecked() && spinnerIPSettings.getSelectedItemPosition() ==
                1) {
            // static ip address info
            networkType = WifiSettings.NETWORK_TYPE_STATIC;
            ipAddress = inputIPAddress.getText().toString();
            if (Strings.isBlank(ipAddress)) {
                isSuccess = false;
                inputIPAddress.setError(getString(R.string.all_error_required));
            } else {
                if (!isValidAddress(ipAddress)) {
                    isSuccess = false;
                    inputIPAddress.setError(getString(R.string.wifi_error_invalid_address));
                }
            }
            netmask = inputNetmask.getText().toString();
            if (Strings.isBlank(netmask)) {
                isSuccess = false;
                inputNetmask.setError(getString(R.string.all_error_required));
            } else {
                if (!isValidAddress(netmask)) {
                    isSuccess = false;
                    inputNetmask.setError(getString(R.string.wifi_error_invalid_address));
                }
            }
            gateway = inputGateway.getText().toString();
            if (Strings.isBlank(gateway)) {
                isSuccess = false;
                inputGateway.setError(getString(R.string.all_error_required));
            } else {
                if (!isValidAddress(gateway)) {
                    isSuccess = false;
                    inputGateway.setError(getString(R.string.wifi_error_invalid_address));
                }
            }
            dns = inputDns.getText().toString();
            if (Strings.isBlank(dns)) {
                isSuccess = false;
                inputDns.setError(getString(R.string.all_error_required));
            } else {
                if (!isValidAddress(dns)) {
                    isSuccess = false;
                    inputDns.setError(getString(R.string.wifi_error_invalid_address));
                }
            }
        }

        if (isSuccess) {
            presenter.onClickConnectWifi(ssid, password, positionSecurity, networkType,
                    ipAddress, netmask, gateway, dns);
            dismissAllowingStateLoss();
        } else {
            Toasts.showLong(R.string.all_error_fill_in);
        }
    }

    private void showHideAddressInfo() {
        int position = spinnerIPSettings.getSelectedItemPosition();
        if (position == 0) {
            // dhcp
            viewAddressInfo.setVisibility(View.GONE);
        } else if (position == 1) {
            // static
            viewAddressInfo.setVisibility(View.VISIBLE);
        }
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(mRunScroll);
    }

    private boolean isValidAddress(@NonNull String address) {
        return address.matches(IPADDRESS_PATTERN);
    }

    private Runnable mRunScroll = new Runnable() {
        @Override
        public void run() {
            scrollView.fullScroll(View.FOCUS_DOWN);
        }
    };

    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
}
