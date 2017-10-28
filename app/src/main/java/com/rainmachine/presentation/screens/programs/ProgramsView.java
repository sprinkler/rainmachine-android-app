package com.rainmachine.presentation.screens.programs;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.main.MainActivity;
import com.rainmachine.presentation.screens.programdetails.ProgramDetailsActivity;
import com.rainmachine.presentation.screens.programdetailsold.ProgramDetailsOldActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.widgets.ItemOffsetDecoration;

import org.joda.time.LocalDateTime;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProgramsView extends ViewFlipper implements ProgramsContract.View {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    ProgramsContract.Presenter presenter;
    @Inject
    CalendarFormatter calendarFormatter;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private ProgramsController controller;

    public ProgramsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            presenter.attachView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            presenter.init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
    }

    @OnClick(R.id.btn_add_program)
    public void onAddProgram() {
        presenter.onClickAddProgram();
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.onClickRetry();
    }

    @Override
    public void render(ProgramsState state) {
        if (state.initialize) {
            initialize();
        }
        if (state.showMoreDialog) {
            showMoreDialog(state);
        } else if (state.isProgress) {
            showProgress();
        } else if (state.isError) {
            showError();
        } else if (state.isContent) {
            controller.setData(state);
            showContent();
        }
    }

    @Override
    public void goToEditScreen(Program program, LocalDateTime sprinklerLocalDateTime,
                               boolean use24HourFormat, boolean isUnitsMetric,
                               boolean showNewProgramDetailsScreen) {
        if (showNewProgramDetailsScreen) {
            getActivity().startActivity(ProgramDetailsActivity.getStartIntent(getActivity(),
                    program, sprinklerLocalDateTime, isUnitsMetric, use24HourFormat));
        } else {
            getActivity().startActivity(ProgramDetailsOldActivity.getStartIntent(getActivity(),
                    program, sprinklerLocalDateTime, use24HourFormat));
        }
    }

    @Override
    public String getNewProgramString() {
        return getActivity().getString(R.string.programs_new_program);
    }

    @Override
    public String getCopyProgramString(String originalProgramName) {
        return getActivity().getString(R.string.programs_copy, originalProgramName);
    }

    private void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    private void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    private void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }

    private void showMoreDialog(ProgramsState state) {
        MoreProgramActionsExtra extra = new MoreProgramActionsExtra(state.dialogMoreProgram,
                state.sprinklerLocalDateTime, state.isUnitsMetric, state.use24HourFormat);
        getActivity().showDialogSafely(MoreProgramActionsDialogFragment.newInstance(extra));
        presenter.onShowingEditMoreDialog();
    }

    private void initialize() {
        int offset = getContext().getResources().getDimensionPixelSize(R.dimen
                .spacing_between_items);
        recyclerView.addItemDecoration(new ItemOffsetDecoration(0, offset, 0, offset));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        controller = new ProgramsController(getContext(), presenter, calendarFormatter);
        recyclerView.setAdapter(controller.getAdapter());
    }

    private MainActivity getActivity() {
        return (MainActivity) getContext();
    }
}
