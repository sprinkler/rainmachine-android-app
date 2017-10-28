package com.rainmachine.presentation.screens.wizardtimezone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimezoneAdapter extends GenericListAdapter<String> implements Filterable {

    private final List<String> allItems;
    private Filter searchFilter;

    public TimezoneAdapter(Context context, List<String> items) {
        super(context, items);
        allItems = items;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View convertView = inflater.inflate(R.layout.item_timezone, container, false);
        ViewHolder holder = new ViewHolder(convertView);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String timezone = getItem(position);
        holder.text.setText(timezone);
    }

    @Override
    public Filter getFilter() {
        if (searchFilter == null) {
            searchFilter = new SearchFilter();
        }
        return searchFilter;
    }

    static class ViewHolder {
        @BindView(R.id.tv_timezone)
        public TextView text;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private class SearchFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (!Strings.isBlank(constraint)) {
                List<String> items = new ArrayList<>();
                for (String timezone : allItems) {
                    if (timezone.toLowerCase(Locale.ENGLISH).contains(constraint)) {
                        items.add(timezone);
                    }
                }
                results.values = items;
                results.count = items.size();
            } else {
                List<String> items = new ArrayList<>(allItems);
                results.values = items;
                results.count = items.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            items = (List<String>) results.values;
            notifyDataSetChanged();
        }
    }
}
