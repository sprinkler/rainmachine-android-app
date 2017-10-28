package com.rainmachine.presentation.screens.wifi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WifiView extends ViewFlipper implements WifiContract.View, AdapterView
        .OnItemClickListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;

    @Inject
    WifiContract.Presenter presenter;

    @BindView(android.R.id.list)
    ListView list;

    private WifiItemAdapter adapter;

    public WifiView(Context context, AttributeSet attrs) {
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WifiItemViewModel ap = adapter.getItem(position);
        presenter.onClickWifi(ap);
    }

    @OnClick(R.id.btn_add_network)
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_add_network) {
            presenter.onClickAddNetwork();
        }
    }

    @Override
    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    @Override
    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    @Override
    public void render(WifiViewModel viewModel) {
        adapter.setItems(viewModel.items);
    }

    @Override
    public void setup() {
        adapter = new WifiItemAdapter(getContext(), new ArrayList<>());
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
    }
}
