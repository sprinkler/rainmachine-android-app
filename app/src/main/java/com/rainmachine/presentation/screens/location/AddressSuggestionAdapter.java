package com.rainmachine.presentation.screens.location;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.Autocomplete;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class AddressSuggestionAdapter extends GenericListAdapter<Autocomplete> implements Filterable {

    private List<String> suggestions;

    AddressSuggestionAdapter(Context context, List<Autocomplete> items) {
        super(context, items);
        updateSuggestions();
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View convertView = this.inflater.inflate(R.layout.item_address_suggestion, container,
                false);
        ViewHolder holder = new ViewHolder(convertView);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        Autocomplete location = getItem(position);
        holder.address.setText(location.description);
    }

    public void setItems(List<Autocomplete> items) {
        this.items = items;
        updateSuggestions();
        notifyDataSetChanged();
    }

    private void updateSuggestions() {
        suggestions = new ArrayList<>();
        for (Autocomplete location : items) {
            suggestions.add(location.description);
        }
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private Filter mFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            filterResults.values = suggestions;
            filterResults.count = suggestions.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
        }
    };

    static class ViewHolder {
        @BindView(R.id.address)
        TextView address;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
