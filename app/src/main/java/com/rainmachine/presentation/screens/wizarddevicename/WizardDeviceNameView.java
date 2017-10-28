package com.rainmachine.presentation.screens.wizarddevicename;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Toasts;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class WizardDeviceNameView extends ViewFlipper {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;

    @Inject
    WizardDeviceNamePresenter presenter;

    @BindView(R.id.input_device_name)
    EditText inputDeviceName;
    @BindView(R.id.input_old)
    EditText inputOldPass;
    @BindView(R.id.input_new)
    EditText inputNewPass;
    @BindView(R.id.input_confirm)
    EditText inputConfirmPass;

    public WizardDeviceNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            presenter.attachView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            presenter.init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
    }

    @OnCheckedChanged(R.id.check_show_password)
    public void onCheckedChanged(boolean isChecked) {
        inputNewPass.setInputType(InputType.TYPE_CLASS_TEXT | (isChecked ? InputType
                .TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType
                .TYPE_TEXT_VARIATION_PASSWORD));
        inputNewPass.setSelection(inputNewPass.getText().length());
        inputConfirmPass.setInputType(InputType.TYPE_CLASS_TEXT | (isChecked ? InputType
                .TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType
                .TYPE_TEXT_VARIATION_PASSWORD));
        inputConfirmPass.setSelection(inputConfirmPass.getText().length());
    }

    @OnClick(R.id.btn_save)
    public void onSave() {
        boolean isSuccess = true;
        String deviceName = inputDeviceName.getText().toString();
        if (Strings.isBlank(deviceName)) {
            isSuccess = false;
            inputDeviceName.setError(getContext().getString(R.string.all_error_required));
        }

        String oldPass = null;
        if (presenter.showOldPass()) {
            oldPass = inputOldPass.getText().toString();
            /*if (Strings.isBlank(oldPass)) {
                isSuccess = false;
                inputOldPass.setError(getString(R.string.error_required));
            }*/
        }

        String newPass = inputNewPass.getText().toString();
        if (Strings.isBlank(newPass)) {
            isSuccess = false;
            inputNewPass.setError(getContext().getString(R.string.all_error_required));
        }

        String confirmPass = inputConfirmPass.getText().toString();
        if (Strings.isBlank(confirmPass)) {
            isSuccess = false;
            inputNewPass.setError(getContext().getString(R.string.all_error_required));
        }

        if (Strings.isBlank(newPass) || !newPass.equals(confirmPass)) {
            isSuccess = false;
            inputNewPass.setError(getContext().getString(R.string.all_error_password_mismatch));
            inputConfirmPass.setError(getContext().getString(R.string.all_error_password_mismatch));
        }

        if (isSuccess) {
            presenter.onClickSave(deviceName, oldPass, newPass);
        } else {
            Toasts.showLong(R.string.all_error_fill_in);
        }
    }

    public void updateContent(String preFilledPass, boolean showOldPassInput) {
        if (showOldPassInput) {
            inputOldPass.setVisibility(View.VISIBLE);
        } else {
            inputOldPass.setVisibility(View.GONE);
        }
        inputNewPass.setText(preFilledPass);
        inputNewPass.setSelection(inputNewPass.getText().length());
        inputConfirmPass.setText(preFilledPass);
        inputConfirmPass.setSelection(inputConfirmPass.getText().length());
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void setup() {
        inputConfirmPass.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSave();
                return true;
            }
            return false;
        });
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }
}
