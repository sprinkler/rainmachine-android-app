package com.rainmachine.presentation.screens.restrictions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestrictedMonthAdapter extends GenericListAdapter<String> {

    public RestrictedMonthAdapter(Context ctx, List<String> items) {
        super(ctx, items);
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View row = inflater.inflate(R.layout.item_restricted_month, container, false);
        ViewHolder holder = new ViewHolder(row);
        row.setTag(holder);
        return row;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String month = getItem(position);
        holder.month.setText(month);
    }

    static class ViewHolder {
        @BindView(R.id.month)
        TextView month;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
