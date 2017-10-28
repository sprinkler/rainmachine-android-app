package com.rainmachine.presentation.screens.help;

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

class HelpAdapter extends GenericRecyclerAdapter<AdapterItemType, HelpAdapter.ViewHolder> {

    private HelpContract.Presenter presenter;

    HelpAdapter(Context context, HelpContract.Presenter presenter, List<AdapterItemType> items) {
        super(context, items);
        this.presenter = presenter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View convertView = inflater.inflate(R.layout.item_help, viewGroup, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AdapterItemType item = getItem(position);
        holder.name.setText(item.getTitle());
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.name)
        TextView name;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            AdapterItemType item = getItem(getAdapterPosition());
            presenter.onClick(item);
        }
    }
}
