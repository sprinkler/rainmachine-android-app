package com.rainmachine.presentation.screens.cloudaccounts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CloudAccountAdapter extends GenericListAdapter<CloudInfo> implements View
        .OnClickListener {

    private CloudAccountsPresenter presenter;

    public CloudAccountAdapter(Context context, List<CloudInfo> items,
                               CloudAccountsPresenter presenter) {
        super(context, items);
        this.presenter = presenter;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View convertView = this.inflater.inflate(R.layout.item_cloud_account, container, false);
        ViewHolder holder = new ViewHolder(convertView);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        CloudInfo cloudInfo = getItem(position);
        holder.email.setText(cloudInfo.email);
        holder.details.setVisibility(View.GONE);
        holder.edit.setOnClickListener(this);
        holder.edit.setTag(position);
        holder.delete.setOnClickListener(this);
        holder.delete.setTag(position);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int position = (Integer) v.getTag();
        CloudInfo cloudInfo = getItem(position);
        if (id == R.id.edit) {
            presenter.onClickEdiCloudAccount(cloudInfo);
        } else if (id == R.id.delete) {
            presenter.onClickDeleteCloudAccount(cloudInfo);
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position)._id;
    }

    static class ViewHolder {
        @BindView(R.id.email)
        TextView email;
        @BindView(R.id.details)
        TextView details;
        @BindView(R.id.edit)
        ImageView edit;
        @BindView(R.id.delete)
        ImageView delete;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
