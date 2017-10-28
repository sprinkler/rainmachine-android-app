package com.rainmachine.presentation.screens.settings;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.util.adapter.GenericRecyclerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class SettingAdapter extends GenericRecyclerAdapter<AdapterItemSetting, SettingAdapter
        .ViewHolder> {

    private SettingsPresenter presenter;

    SettingAdapter(Context context, SettingsPresenter presenter, List<AdapterItemSetting> items) {
        super(context, items);
        this.presenter = presenter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View convertView = inflater.inflate(R.layout.item_setting, viewGroup, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AdapterItemSetting setting = getItem(position);
        if (setting.useOneLine) {
            holder.name.setText(setting.name);
            holder.description.setVisibility(View.GONE);
        } else {
            holder.name.setText(setting.name);
            holder.description.setText(setting.description);
            holder.description.setVisibility(View.VISIBLE);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.description)
        TextView description;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            presenter.onClick(getItem(getAdapterPosition()));
        }
    }
}
