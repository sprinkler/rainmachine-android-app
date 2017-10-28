package com.rainmachine.presentation.screens.hours;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.HourlyRestrictionFormatter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HoursView extends ViewFlipper implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    HoursPresenter presenter;
    @Inject
    CalendarFormatter calendarFormatter;
    @Inject
    HourlyRestrictionFormatter hourlyRestrictionFormatter;

    @BindView(android.R.id.list)
    ListView list;

    private HourAdapter adapter;

    public HoursView(Context context, AttributeSet attrs) {
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
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        presenter.onItemClick(position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        return presenter.onItemLongClick(position);
    }

    @OnClick(R.id.btn_add_restriction)
    public void onAddRestriction() {
        presenter.onClickAddRestriction();
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.onClickRetry();
    }

    public void setup() {
        adapter = new HourAdapter(getContext(), new ArrayList<>(),
                calendarFormatter, hourlyRestrictionFormatter);
        list.setAdapter(adapter);

        list.setItemsCanFocus(false);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);
    }

    public void onActionModeClicked() {
        SparseBooleanArray items = list.getCheckedItemPositions();
        List<Long> uids = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (items.valueAt(i)) {
                long pid = list.getItemIdAtPosition(items.keyAt(i));
                uids.add(pid);
            }
        }
        if (uids.size() > 0) {
            presenter.onDeleteHourlyRestrictions(uids);
        }
    }

    public void checkItem(int position, boolean checked) {
        list.setItemChecked(position, checked);
    }

    public void uncheckAllItems() {
        for (int i = 0; i < adapter.getCount(); i++) {
            list.setItemChecked(i, false);
        }
    }

    public void updateContent(HoursViewModel viewModel) {
        adapter.setUse24HourFormat(viewModel.use24HourFormat);
        adapter.setItems(viewModel.hourlyRestrictions);
    }

    public HourlyRestriction getItem(int position) {
        return adapter.getItem(position);
    }

    public SparseBooleanArray getCheckedItems() {
        return list.getCheckedItemPositions();
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
