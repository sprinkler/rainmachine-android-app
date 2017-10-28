package com.rainmachine.presentation.screens.directaccess;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.presentation.activities.NonSprinklerActivity;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DirectAccessView extends LinearLayout {

    @Inject
    protected DirectAccessPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private DirectAccessAdapter adapter;

    public DirectAccessView(Context context, AttributeSet attrs) {
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

    @OnClick(R.id.btn_add_device)
    public void onAddManualDevice() {
        presenter.onClickAddManualDevice();
    }

    public void render(List<Device> items) {
        adapter.setItems(items);
    }

    public void setup() {
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), null, true));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        adapter = new DirectAccessAdapter(getContext(), new ArrayList<>(), presenter);
        recyclerView.setAdapter(adapter);
    }
}
