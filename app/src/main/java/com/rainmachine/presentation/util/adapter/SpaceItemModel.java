package com.rainmachine.presentation.util.adapter;

import android.support.v4.widget.Space;
import android.view.View;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.rainmachine.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpaceItemModel extends EpoxyModelWithHolder<SpaceItemModel.ViewHolder> {

    @EpoxyAttribute
    int spacing;

    @Override
    protected ViewHolder createNewHolder() {
        return new ViewHolder();
    }

    @Override
    public void bind(ViewHolder holder) {
        holder.space.setMinimumHeight(spacing);
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_space;
    }

    class ViewHolder extends EpoxyHolder {

        @BindView(R.id.space)
        Space space;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
