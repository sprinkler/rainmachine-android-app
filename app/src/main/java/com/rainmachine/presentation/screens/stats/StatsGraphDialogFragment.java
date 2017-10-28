package com.rainmachine.presentation.screens.stats;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.View;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.activities.SprinklerActivity;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StatsGraphDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.toggle_enabled)
    SwitchCompat toggleEnabled;
    @BindView(R.id.edit_program)
    View viewEditProgram;

    public interface Callback {
        void onDialogStatsGraphPositiveClick();

        void onDialogStatsGraphShowAllData(DashboardGraphs.DashboardGraph graph, String viewType);

        void onDialogStatsGraphEditProgram(Program program);
    }

    public static StatsGraphDialogFragment newInstance(String title, String positiveBtn,
                                                       DashboardGraphs.DashboardGraph graph,
                                                       String viewType, Program program) {
        StatsGraphDialogFragment fragment = new StatsGraphDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("positiveBtn", positiveBtn);
        args.putParcelable("graph", Parcels.wrap(graph));
        args.putString("viewType", viewType);
        args.putParcelable("program", Parcels.wrap(program));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((SprinklerActivity) getActivity()).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString("title"));

        View view = View.inflate(getContext(), R.layout.dialog_stats_graph, null);
        ButterKnife.bind(this, view);
        DashboardGraphs.DashboardGraph graph = Parcels.unwrap(getArguments().getParcelable
                ("graph"));
        toggleEnabled.setChecked(graph.isEnabled);
        toggleEnabled.setEnabled(graph.graphType != DashboardGraphs.GraphType.WEATHER);
        viewEditProgram.setVisibility(graph.graphType == DashboardGraphs.GraphType.PROGRAM ? View
                .VISIBLE : View.GONE);
        builder.setView(view);

        builder.setPositiveButton(getArguments().getString("positiveBtn"), (dialog, id) -> {
            DashboardGraphs.DashboardGraph graph1 = Parcels.unwrap(getArguments().getParcelable
                    ("graph"));
            graph1.isEnabled = toggleEnabled.isChecked();
            callback.onDialogStatsGraphPositiveClick();
        });
        return builder.create();
    }

    @OnClick({R.id.show_all_data, R.id.label, R.id.edit_program})
    void onClick(View view) {
        int id = view.getId();
        DashboardGraphs.DashboardGraph graph = Parcels.unwrap(getArguments().getParcelable
                ("graph" + ""));
        if (id == R.id.show_all_data) {
            String viewType = getArguments().getString("viewType");
            callback.onDialogStatsGraphShowAllData(graph, viewType);
            dismissAllowingStateLoss();
        } else if (id == R.id.label) {
            if (graph.graphType != DashboardGraphs.GraphType.WEATHER) {
                toggleEnabled.toggle();
            }
        } else if (id == R.id.edit_program) {
            callback.onDialogStatsGraphEditProgram(getProgram());
        }
    }

    private Program getProgram() {
        return Parcels.unwrap(getArguments().getParcelable("program"));
    }
}
