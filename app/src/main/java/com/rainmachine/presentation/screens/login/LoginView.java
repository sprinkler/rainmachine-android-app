package com.rainmachine.presentation.screens.login;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.PresentationUtils;
import com.rainmachine.presentation.util.Toasts;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginView extends ViewFlipper {

    private final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;

    @Inject
    LoginPresenter presenter;
    @Inject
    Features features;

    @BindView(R.id.input_password)
    EditText inputPassword;
    @BindView(R.id.check_remember)
    CheckBox checkRemember;
    @BindView(R.id.input_username)
    EditText inputUsername;

    public LoginView(Context context, AttributeSet attrs) {
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

    @OnClick(R.id.btn_login)
    public void onLogin() {
        doLogin();
    }

    private void doLogin() {
        boolean isSuccess = true;
        String username = null;
        if (features.showUsername()) {
            username = inputUsername.getText().toString();
            if (Strings.isBlank(username)) {
                isSuccess = false;
                inputUsername.setError(getContext().getString(R.string.all_error_required));
            }
        }

        String pass = inputPassword.getText().toString();
        if (!features.canBeEmptyPassword()) {
            if (Strings.isBlank(pass)) {
                isSuccess = false;
                inputPassword.setError(getContext().getString(R.string.all_error_required));
            }
        }
        boolean isRemember = checkRemember.isChecked();

        if (isSuccess) {
            PresentationUtils.hideSoftKeyboard(inputPassword);
            presenter.onClickLogin(username, pass, isRemember);
        } else {
            Toasts.showLong(R.string.all_error_fill_in);
        }
    }

    public void updateContent(String username) {
        if (features.showUsername()) {
            inputUsername.setVisibility(View.VISIBLE);
            inputUsername.setText(username);
            inputUsername.setSelection(inputUsername.length());
        } else {
            inputUsername.setVisibility(View.GONE);
        }
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    public void setup() {
        inputPassword.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onLogin();
                return true;
            }
            return false;
        });
    }

    public void showKeyboard() {
        MAIN_HANDLER.postDelayed(mRunShowKeyboard, 300);
    }

    public void cancelShowKeyboard() {
        MAIN_HANDLER.removeCallbacks(mRunShowKeyboard);
    }

    private Runnable mRunShowKeyboard = new Runnable() {
        @Override
        public void run() {
            PresentationUtils.showSoftKeyboard(inputPassword);
        }
    };
}
