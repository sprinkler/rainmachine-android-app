package com.rainmachine.presentation.screens.devices;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.presentation.activities.NonSprinklerActivity;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DevicesView extends FrameLayout implements DevicesContract.View {

    @Inject
    protected DevicesContract.Presenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.empty)
    TextView empty;

    private DeviceAdapter adapter;

    public DevicesView(Context context, AttributeSet attrs) {
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

    @Override
    public void updateCurrentWifiMac(String currentWifiMac) {
        adapter.setCurrentWifiMac(currentWifiMac);
    }

    @Override
    public void render(List<Device> items) {
        adapter.setItems(items);
        empty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmptyScreen() {
        recyclerView.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);
    }

    @Override
    public void setup() {
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), null, true));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        adapter = new DeviceAdapter(getContext(), new ArrayList<>(), presenter);
        recyclerView.setAdapter(adapter);
    }
}
