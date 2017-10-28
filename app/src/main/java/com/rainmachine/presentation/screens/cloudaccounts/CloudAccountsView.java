package com.rainmachine.presentation.screens.cloudaccounts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.NonSprinklerActivity;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CloudAccountsView extends ViewFlipper {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;

    @Inject
    protected CloudAccountsPresenter presenter;

    @BindView(android.R.id.list)
    ListView list;

    private CloudAccountAdapter adapter;

    public CloudAccountsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((NonSprinklerActivity) getContext()).inject(this);
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

    @OnClick(R.id.btn_add_cloud_account)
    public void onAddCloudAccount() {
        presenter.onClickAddAccount();
    }

    public void render(CloudAccountsViewModel viewModel) {
        adapter.setItems(viewModel.cloudInfoList);
    }

    public void setup() {
        adapter = new CloudAccountAdapter(getContext(), new ArrayList<>(), presenter);
        list.setAdapter(adapter);
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }
}
