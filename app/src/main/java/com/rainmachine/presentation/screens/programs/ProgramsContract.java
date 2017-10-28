package com.rainmachine.presentation.screens.programs;

import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.util.ElmPresenter;

import org.joda.time.LocalDateTime;

public interface ProgramsContract {

    interface View {

        void render(ProgramsState state);

        void goToEditScreen(Program program, LocalDateTime sprinklerLocalDateTime, boolean
                use24HourFormat, boolean isUnitsMetric, boolean showNewProgramDetailsScreen);

        String getNewProgramString();

        String getCopyProgramString(String originalProgramName);
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>, ElmPresenter {

        void start();

        void onClickRetry();

        void onClickAddProgram();

        void onClickStartStop(Program program);

        void onClickEditMore(Program program);

        void onShowingEditMoreDialog();

        void onClickEdit(MoreProgramActionsExtra extra);

        void onClickClone(MoreProgramActionsExtra extra);

        void onClickDelete(MoreProgramActionsExtra extra);

        void onClickActivateDeactivate(MoreProgramActionsExtra extra);
    }
}
