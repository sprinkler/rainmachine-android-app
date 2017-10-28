package com.rainmachine.presentation.screens.dashboardgraphs;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DashboardGraphsView extends ViewFlipper {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    DashboardGraphsPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private DashboardGraphsController controller;

    public DashboardGraphsView(Context context, AttributeSet attrs) {
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
        presenter.onClickRetry();
    }

    void setup() {
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), null, true));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        controller = new DashboardGraphsController(presenter);
        recyclerView.setAdapter(controller.getAdapter());
    }

    void render(DashboardGraphsViewModel viewModel) {
        controller.setData(viewModel);
    }

    void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }
}
