package com.rainmachine.presentation.screens.softwareupdate;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
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

public class SoftwareUpdateView extends ViewFlipper {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    SoftwareUpdatePresenter presenter;

    @BindView(R.id.progress_text)
    TextView progressText;
    @BindView(R.id.tv_update)
    TextView tvUpdate;
    @BindView(R.id.btn_update)
    Button btnUpdate;

    public SoftwareUpdateView(Context context, AttributeSet attrs) {
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

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.onRetry();
    }

    @OnClick(R.id.btn_update)
    public void onUpdate() {
        presenter.onClickUpdate();
    }

    void updateProgress(String text) {
        progressText.setText(text);
    }

    public void render(SoftwareUpdateViewModel viewModel) {
        CharSequence text;
        String currentVersion = viewModel.update.currentVersion;
        if (viewModel.update.update) {
            if (!Strings.isBlank(viewModel.update.newVersion)) {
                Truss truss = new Truss()
                        .append(getContext().getString(R.string.software_update_new_update_found))
                        .pushSpan(new StyleSpan(Typeface.ITALIC))
                        .append(" v")
                        .append(viewModel.update.newVersion)
                        .popSpan()
                        .append("\n")
                        .append(getContext().getString(R.string.software_update_current_version))
                        .pushSpan(new StyleSpan(Typeface.ITALIC))
                        .append(" v")
                        .append(currentVersion)
                        .popSpan();
                text = truss.build();
            } else {
                Truss truss = new Truss()
                        .append(getContext().getString(R.string
                                .software_update_new_update_found_current_version))
                        .pushSpan(new StyleSpan(Typeface.ITALIC))
                        .append(" v")
                        .append(currentVersion)
                        .popSpan();
                text = truss.build();
            }
            btnUpdate.setVisibility(View.VISIBLE);
        } else {
            Truss truss = new Truss()
                    .append(getContext().getString(R.string.software_update_no_updates_found))
                    .pushSpan(new StyleSpan(Typeface.ITALIC))
                    .append(" v")
                    .append(currentVersion)
                    .popSpan();
            text = truss.build();

            btnUpdate.setVisibility(View.GONE);
        }
        tvUpdate.setText(text);
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    public void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }
}
