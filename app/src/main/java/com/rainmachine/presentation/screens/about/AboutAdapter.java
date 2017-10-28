package com.rainmachine.presentation.screens.about;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.util.adapter.GenericRecyclerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class AboutAdapter extends GenericRecyclerAdapter<AdapterItemAbout, AboutAdapter.ViewHolder> {

    private AboutContract.Presenter presenter;
    private int numConsecutiveClicksSupport;

    AboutAdapter(Context context, AboutContract.Presenter presenter, List<AdapterItemAbout> items) {
        super(context, items);
        this.presenter = presenter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View convertView = inflater.inflate(R.layout.item_about, viewGroup, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AdapterItemAbout setting = getItem(position);
        holder.name.setText(setting.name);
        holder.description.setText(setting.description);
        if (setting.isUpdatable) {
            holder.update.setVisibility(View.VISIBLE);
            holder.update.setOnClickListener(holder);
        } else {
            holder.update.setVisibility(View.GONE);
            holder.update.setOnClickListener(null);
        }
        holder.view.setOnClickListener(setting.isClickableForSupport ? holder : null);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.btn_update)
        Button update;

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
                AdapterItemAbout setting = getItem(adapterPosition);
                if (setting.isUpdatable) {
                    presenter.onClickUpdate();
                } else if (setting.isClickableForSupport) {
                    numConsecutiveClicksSupport++;
                    if (numConsecutiveClicksSupport >= 5) {
                        presenter.onConsecutiveClicksSupport();
                        numConsecutiveClicksSupport = 0;
                    }
                }
            }
        }
    }
}
