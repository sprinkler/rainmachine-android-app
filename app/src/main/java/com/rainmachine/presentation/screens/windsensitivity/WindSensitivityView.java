package com.rainmachine.presentation.screens.windsensitivity;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WindSensitivityView extends ViewFlipper implements SeekBar.OnSeekBarChangeListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    WindSensitivityPresenter presenter;

    @BindView(R.id.seekbar)
    SeekBar seekBar;

    public WindSensitivityView(Context context, AttributeSet attrs) {
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        presenter.onProgressChanged(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.onRetry();
    }

    public void setup() {
        seekBar.setMax(100);
    }

    public void render(WindSensitivityViewModel viewModel) {
        updateSeekBar(viewModel.windSensitivity);
    }

    void updateSeekBar(float rainSensitivity) {
        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setProgress((int) (rainSensitivity * 100));
        seekBar.setOnSeekBarChangeListener(this);
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }
}
