package com.rainmachine.presentation.screens.wateringduration;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class WateringDurationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SECTION_NAME = 0;
    private static final int TYPE_SECTION_LIST = 1;

    private final Context context;
    private CalendarFormatter calendarFormatter;
    private List<SectionViewModel> items;
    private final WateringDurationContract.Presenter presenter;
    private final LayoutInflater inflater;

    WateringDurationAdapter(Context context, CalendarFormatter calendarFormatter,
                            List<SectionViewModel> items,
                            WateringDurationContract.Presenter presenter) {
        this.context = context;
        this.calendarFormatter = calendarFormatter;
        this.items = items;
        this.presenter = presenter;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION_NAME) {
            View convertView = inflater.inflate(R.layout.item_section_name, parent, false);
            return new ViewHolderSectionName(convertView);
        } else {
            View convertView = inflater.inflate(R.layout.item_section_list, parent, false);
            return new ViewHolderSectionList(convertView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();
        SectionViewModel sectionViewModel = getItem(position);
        if (viewType == TYPE_SECTION_NAME) {
            ViewHolderSectionName viewHolder = (ViewHolderSectionName) holder;
            viewHolder.sectionName.setText(sectionName(sectionViewModel.type));
        } else if (viewType == TYPE_SECTION_LIST) {
            ViewHolderSectionList viewHolder = (ViewHolderSectionList) holder;
            int textColor = ContextCompat.getColor(context, sectionViewModel.type ==
                    SectionViewModel.Type.ACTIVE
                    ? R.color.selector_color_text_primary : R.color.text_gray);
            viewHolder.setAdapter(new WateringDurationOneSectionAdapter(context, calendarFormatter,
                    sectionViewModel.zones, textColor, presenter));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0) {
            return TYPE_SECTION_NAME;
        } else {
            return TYPE_SECTION_LIST;
        }
    }

    @Override
    public int getItemCount() {
        return 2 * items.size();
    }

    public SectionViewModel getItem(int position) {
        return items.get(position / 2);
    }

    public void setItems(List<SectionViewModel> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    private String sectionName(SectionViewModel.Type type) {
        if (type == SectionViewModel.Type.ACTIVE) {
            return context.getString(R.string.watering_duration_active_zones);
        } else if (type == SectionViewModel.Type.INACTIVE) {
            return context.getString(R.string.watering_duration_inactive_zones);
        }
        throw new IllegalArgumentException("Not supported section type " + type);
    }

    @SuppressWarnings("WeakerAccess")
    class ViewHolderSectionName extends RecyclerView.ViewHolder {
        @BindView(R.id.section_name)
        TextView sectionName;

        ViewHolderSectionName(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @SuppressWarnings("WeakerAccess")
    class ViewHolderSectionList extends RecyclerView.ViewHolder {
        @BindView(R.id.recycler_section_list)
        RecyclerView recyclerSectionList;

        private WateringDurationOneSectionAdapter sectionAdapter;

        ViewHolderSectionList(View view) {
            super(view);
            ButterKnife.bind(this, view);
            recyclerSectionList.addItemDecoration(new DividerItemDecoration(ContextCompat
                    .getDrawable(context, android.R.drawable.divider_horizontal_textfield)));
            recyclerSectionList.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.VERTICAL, false));
            recyclerSectionList.setBackgroundColor(ContextCompat.getColor(context, android.R
                    .color.white));
        }

        void setAdapter(WateringDurationOneSectionAdapter adapter) {
            sectionAdapter = adapter;
            recyclerSectionList.setAdapter(sectionAdapter);
        }
    }
}
