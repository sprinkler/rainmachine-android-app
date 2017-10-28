package com.rainmachine.presentation.screens.directaccess;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.remote.util.RemoteUtils;
import com.rainmachine.presentation.util.adapter.GenericRecyclerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DirectAccessAdapter extends GenericRecyclerAdapter<Device, DirectAccessAdapter
        .ViewHolder> {

    private DirectAccessPresenter presenter;

    public DirectAccessAdapter(Context ctx, List<Device> items, DirectAccessPresenter presenter) {
        super(ctx, items);
        this.presenter = presenter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View convertView = inflater.inflate(R.layout.item_direct_access, viewGroup, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Device device = getItem(position);
        holder.name.setText(device.name);
        holder.url.setText(RemoteUtils.getDomainNameFromUrl(device.getUrl()));
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.url)
        TextView url;
        @BindView(R.id.edit)
        ImageView edit;
        @BindView(R.id.delete)
        ImageView delete;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            edit.setOnClickListener(this);
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Device device = getItem(adapterPosition);
                int id = v.getId();
                if (id == R.id.edit) {
                    presenter.onClickEditManualDevice(device);
                } else if (id == R.id.delete) {
                    presenter.onClickDeleteManualDevice(device);
                }
            }
        }
    }
}
