package com.rainmachine.presentation.screens.nearbystations;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.presentation.util.adapter.GenericRecyclerAdapter;
import com.rainmachine.presentation.widgets.CheckableLinearLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class NearbyStationAdapter extends GenericRecyclerAdapter<Parser.WeatherStation,
        NearbyStationAdapter.ViewHolder> {

    private NearbyStationsPresenter presenter;

    private int checkedPosition;

    NearbyStationAdapter(Context context, List<Parser.WeatherStation> items,
                         NearbyStationsPresenter presenter) {
        super(context, items);
        this.presenter = presenter;
        checkedPosition = -1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = inflater.inflate(R.layout.item_nearby_station, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Parser.WeatherStation station = getItem(position);
        String stationName;
        if (station.hasIncompleteInfo) {
            stationName = station.name;
        } else {
            stationName = station.name + " (" + station.distance + " km)";
        }
        holder.name.setText(stationName);
        holder.viewItem.setChecked(position == checkedPosition);
    }

    void setItemChecked(int position) {
        checkedPosition = position;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.view_row)
        CheckableLinearLayout viewItem;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                setItemChecked(adapterPosition);
                Parser.WeatherStation station = getItem(adapterPosition);
                presenter.onClickStation(station);
            }
        }
    }
}
