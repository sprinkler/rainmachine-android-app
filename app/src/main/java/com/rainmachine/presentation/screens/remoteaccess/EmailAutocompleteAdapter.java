package com.rainmachine.presentation.screens.remoteaccess;

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

import butterknife.BindView;
import butterknife.ButterKnife;

public class EmailAutocompleteAdapter extends GenericListAdapter<String> implements Filterable {

    private List<String> emails;

    public EmailAutocompleteAdapter(Context context, List<String> emails) {
        super(context, emails);
        this.emails = emails;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View convertView = this.inflater.inflate(R.layout.item_email_suggestion, container, false);
        ViewHolder holder = new ViewHolder(convertView);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String email = getItem(position);
        holder.email.setText(email);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (!Strings.isBlank(constraint)) {
                List<String> suggestions = new ArrayList<>();
                for (String email : emails) {
                    if (email.startsWith(constraint.toString())) {
                        suggestions.add(email);
                    }
                }
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
            } else {
                filterResults.values = emails;
                filterResults.count = emails.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            setItems((List<String>) results.values);
        }
    };

    static class ViewHolder {
        @BindView(R.id.email)
        TextView email;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
