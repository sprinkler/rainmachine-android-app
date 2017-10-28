package com.rainmachine.presentation.screens.programs;

import android.content.Context;
import android.view.View;

import com.airbnb.epoxy.OnModelClickListener;
import com.airbnb.epoxy.TypedEpoxyController;
import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.util.adapter.SpaceItemModel_;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

class ProgramsController extends TypedEpoxyController<ProgramsState> {

    private Context context;
    private ProgramsContract.Presenter presenter;
    private CalendarFormatter formatter;

    ProgramsController(Context context, ProgramsContract.Presenter presenter, CalendarFormatter
            formatter) {
        this.context = context;
        this.presenter = presenter;
        this.formatter = formatter;
    }

    @Override
    protected void buildModels(ProgramsState viewModel) {
        for (Program program : viewModel.enabledPrograms) {
            new ProgramItemModel_(context, formatter)
                    .id(program.id)
                    .item(program)
                    .handPreference(viewModel.handPreference)
                    .use24HourFormat(viewModel.use24HourFormat)
                    .clickListener(clickListener)
                    .addTo(this);
        }

        new SpaceItemModel_()
                .id("spacing")
                .spacing(context.getResources().getDimensionPixelSize(R.dimen.spacing_medium))
                .addTo(this);

        for (Program program : viewModel.disabledPrograms) {
            new ProgramItemModel_(context, formatter)
                    .id(program.id)
                    .item(program)
                    .handPreference(viewModel.handPreference)
                    .use24HourFormat(viewModel.use24HourFormat)
                    .clickListener(clickListener)
                    .addTo(this);
        }
    }

    private OnModelClickListener<ProgramItemModel_, ProgramItemModel.ViewHolder> clickListener
            = new OnModelClickListener<ProgramItemModel_, ProgramItemModel.ViewHolder>() {

        @Override
        public void onClick(ProgramItemModel_ model, ProgramItemModel.ViewHolder
                parentView, View clickedView, int position) {
            int id = clickedView.getId();
            if (id == R.id.img_left) {
                presenter.onClickStartStop(model.item());
            } else if (id == R.id.img_right) {
                presenter.onClickStartStop(model.item());
            } else if (id == R.id.view_name_schedule) {
                presenter.onClickEditMore(model.item());
            }
        }
    };
}
