package com.rainmachine.presentation.screens.remoteaccess;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.SwitchCompat;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Truss;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RemoteAccessView extends ViewFlipper implements CompoundButton
        .OnCheckedChangeListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    protected RemoteAccessPresenter presenter;

    @BindView(R.id.tv_cloud_email)
    TextView tvCloudEmail;
    @BindView(R.id.toggle_cloud_email)
    SwitchCompat toggleCloudEmail;
    @BindView(R.id.btn_send_confirmation)
    Button btnSendConfirmation;

    public RemoteAccessView(Context context, AttributeSet attrs) {
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.toggle_cloud_email) {
            if (isChecked) {
                presenter.onCheckedEnableCloudEmail();
            } else {
                presenter.onCheckedDisableCloudEmail();
            }
        }
    }

    @OnClick(R.id.btn_send_confirmation)
    public void onSendConfirmationEmail() {
        presenter.onSendConfirmationEmail();
    }

    @OnClick(R.id.card_remote_access)
    public void onClickCloudEmail() {
        presenter.onClickCloudEmail();
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.refresh();
    }

    @OnClick(R.id.card_password)
    public void onPassword() {
        presenter.onClickPassword();
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

    public void updateSwitch(RemoteAccessViewModel viewModel) {
        toggleCloudEmail.setOnCheckedChangeListener(null);
        toggleCloudEmail.setChecked(viewModel.cloudEnabled);
        toggleCloudEmail.setOnCheckedChangeListener(this);
    }

    public void render(RemoteAccessViewModel viewModel) {
        if (!Strings.isBlank(viewModel.currentPendingEmail)) {
            Truss truss = new Truss()
                    .append(viewModel.currentPendingEmail)
                    .append(" ")
                    .pushSpan(new StyleSpan(Typeface.ITALIC))
                    .append(getContext().getString(R.string.all_pending))
                    .popSpan();
            tvCloudEmail.setText(truss.build());
            btnSendConfirmation.setVisibility(viewModel.cloudEnabled ? View.VISIBLE : View.GONE);
        } else {
            if (!Strings.isBlank(viewModel.currentEmail)) {
                tvCloudEmail.setText(viewModel.currentEmail);
            } else {
                tvCloudEmail.setText(R.string.all_not_set);
            }
            btnSendConfirmation.setVisibility(View.GONE);
        }

        updateSwitch(viewModel);
    }
}
