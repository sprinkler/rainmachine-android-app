package com.rainmachine.presentation.screens.programs;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.activities.SprinklerActivity;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MoreProgramActionsDialogFragment extends BottomSheetDialogFragment {

    private static final String EXTRA = "program";

    @Inject
    ProgramsContract.Presenter presenter;

    @BindView(R.id.btn_activate)
    TextView btnActivate;

    private BottomSheetBehavior.BottomSheetCallback bottomSheetBehaviorCallback = new
            BottomSheetBehavior.BottomSheetCallback() {

                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismissAllowingStateLoss();
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            };

    public static MoreProgramActionsDialogFragment newInstance(MoreProgramActionsExtra extra) {
        MoreProgramActionsDialogFragment fragment = new MoreProgramActionsDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA, Parcels.wrap(extra));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((SprinklerActivity) getActivity()).inject(this);
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View view = View.inflate(getContext(), R.layout.dialog_more_program_actions, null);
        dialog.setContentView(view);
        ButterKnife.bind(this, view);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View)
                view.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetBehaviorCallback);
            view.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                //noinspection deprecation
                                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                            ((BottomSheetBehavior) behavior).setPeekHeight(view.getMeasuredHeight
                                    ());
                        }
                    });
        }

        MoreProgramActionsExtra extra = Parcels.unwrap(getArguments().getParcelable(EXTRA));
        render(extra.program);
    }

    private void render(Program program) {
        btnActivate.setText(program.enabled ? R.string.programs_deactivate : R.string
                .programs_activate);
    }

    @OnClick({R.id.btn_edit, R.id.btn_clone, R.id.btn_delete, R.id.btn_activate})
    void onClick(View view) {
        int id = view.getId();
        MoreProgramActionsExtra extra = Parcels.unwrap(getArguments().getParcelable(EXTRA));
        if (id == R.id.btn_edit) {
            presenter.onClickEdit(extra);
            dismissAllowingStateLoss();
        } else if (id == R.id.btn_clone) {
            presenter.onClickClone(extra);
            dismissAllowingStateLoss();
        } else if (id == R.id.btn_delete) {
            presenter.onClickDelete(extra);
            dismissAllowingStateLoss();
        } else if (id == R.id.btn_activate) {
            presenter.onClickActivateDeactivate(extra);
            dismissAllowingStateLoss();
        }
    }
}
