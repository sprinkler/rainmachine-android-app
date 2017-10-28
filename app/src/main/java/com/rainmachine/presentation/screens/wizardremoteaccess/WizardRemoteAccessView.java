package com.rainmachine.presentation.screens.wizardremoteaccess;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.util.EmailAddress;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.remoteaccess.EmailAutocompleteAdapter;
import com.rainmachine.presentation.util.PresentationUtils;
import com.rainmachine.presentation.util.Toasts;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WizardRemoteAccessView extends ViewFlipper {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    private static final int THRESHOLD = 1; // characters

    @Inject
    protected WizardRemoteAccessPresenter presenter;

    @BindView(R.id.input_cloud_email)
    AutoCompleteTextView autoComplete;

    public WizardRemoteAccessView(Context context, AttributeSet attrs) {
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

    @OnClick(R.id.btn_save_email)
    public void onSaveEmail() {
        boolean isSuccess = true;
        String cloudEmail = autoComplete.getText().toString().trim();
        if (Strings.isBlank(cloudEmail)) {
            isSuccess = false;
            autoComplete.setError(getContext().getString(R.string.all_error_required));
        } else if (!EmailAddress.isValid(cloudEmail)) {
            isSuccess = false;
            autoComplete.setError(getContext().getString(R.string.all_error_invalid));
        }

        if (isSuccess) {
            presenter.onSaveCloudEmail(cloudEmail);
            PresentationUtils.hideSoftKeyboard(autoComplete);
        } else {
            Toasts.showLong(R.string.all_error_fill_in);
        }
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.refresh();
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }

    public void render(WizardRemoteAccessViewModel viewModel) {
        EmailAutocompleteAdapter adapter = new EmailAutocompleteAdapter(getContext(), viewModel
                .knownEmails);
        autoComplete.setAdapter(adapter);
        autoComplete.setThreshold(THRESHOLD);

        // Pre-fill with first email available
        if (viewModel.knownEmails.size() > 0) {
            autoComplete.setText(viewModel.knownEmails.get(0));
        }
        PresentationUtils.showSoftKeyboard(autoComplete);
    }

    public void setup() {
        autoComplete.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSaveEmail();
                return true;
            }
            return false;
        });
    }
}
