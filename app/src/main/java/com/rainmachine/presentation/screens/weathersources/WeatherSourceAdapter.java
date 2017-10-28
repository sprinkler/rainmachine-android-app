package com.rainmachine.presentation.screens.weathersources;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;
import com.rainmachine.presentation.util.formatter.ParserFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherSourceAdapter extends GenericListAdapter<WeatherSource> {

    private WeatherSourcesPresenter presenter;
    private final ParserFormatter parserFormatter;

    public WeatherSourceAdapter(Context context, List<WeatherSource> items,
                                WeatherSourcesPresenter presenter, ParserFormatter
                                        parserFormatter) {
        super(context, items);
        this.presenter = presenter;
        this.parserFormatter = parserFormatter;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View convertView = this.inflater.inflate(R.layout.item_weather_source, container, false);
        ViewHolder holder = new ViewHolder(convertView);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(Object item, final int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        WeatherSource source = getItem(position);
        holder.name.setText(source.parser.name);
        holder.viewRow.setOnClickListener(v -> {
            WeatherSource source1 = getItem(position);
            int id = v.getId();
            if (id == R.id.view_row) {
                presenter.onClickParser(source1.parser);
            }
        });
        holder.coverArea.setText(getContext().getString(R.string.all_cover_area, parserFormatter
                .coverArea(getContext(), source.parser)));
    }

    static class ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.view_row)
        ViewGroup viewRow;
        @BindView(R.id.cover_area)
        TextView coverArea;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
