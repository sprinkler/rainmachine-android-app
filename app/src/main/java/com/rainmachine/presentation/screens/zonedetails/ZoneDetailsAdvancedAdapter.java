package com.rainmachine.presentation.screens.zonedetails;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.util.adapter.GenericRecyclerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ZoneDetailsAdvancedAdapter extends GenericRecyclerAdapter<ZoneDetailsAdvancedItem,
        ZoneDetailsAdvancedAdapter.ViewHolder> {

    private ZoneDetailsAdvancedItem selectedItem;
    private ZoneDetailsAdvancedPresenter presenter;

    public ZoneDetailsAdvancedAdapter(Context context, List<ZoneDetailsAdvancedItem> items,
                                      ZoneDetailsAdvancedPresenter presenter) {
        super(context, items);
        this.presenter = presenter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = inflater.inflate(R.layout.item_zone_details_advanced, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ZoneDetailsAdvancedItem item = getItem(position);
        holder.text.setText(item.text);
        holder.icon.setImageResource(item.icon);
        if (item.equals(selectedItem)) {
            holder.icon.setSelected(true);
        } else {
            holder.icon.setSelected(false);
        }
        holder.view.setOnClickListener(holder);
    }

    public void setSelectedItem(ZoneDetailsAdvancedItem item) {
        selectedItem = item;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.text)
        TextView text;
        @BindView(R.id.icon)
        ImageView icon;

        View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            ButterKnife.bind(this, view);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                ZoneDetailsAdvancedItem item = getItem(getAdapterPosition());
                setSelectedItem(item);
                presenter.onSelectedItem(item);
            }
        }
    }
}
