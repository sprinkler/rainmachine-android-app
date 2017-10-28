package com.rainmachine.presentation.screens.dashboardgraphs;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;

import java.util.ArrayList;
import java.util.Iterator;

import io.reactivex.Observable;

class DashboardGraphsMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private Features features;

    DashboardGraphsMixer(SprinklerRepositoryImpl sprinklerRepository, Features features) {
        this.sprinklerRepository = sprinklerRepository;
        this.features = features;
    }

    Observable<DashboardGraphsViewModel> refresh() {
        return Observable.combineLatest(
                sprinklerRepository.programs().toObservable(),
                sprinklerRepository.dashboardGraphs().toObservable(),
                (programs, dashboardGraphs) -> {
                    DashboardGraphsViewModel viewModel = new DashboardGraphsViewModel();
                    viewModel.dashboardGraphs = dashboardGraphs;
                    viewModel.generalGraphs = new ArrayList<>();
                    viewModel.programGraphs = new ArrayList<>();

                    for (Program program : programs) {
                        boolean isEntryInDb = false;
                        for (DashboardGraphs.DashboardGraph graph : dashboardGraphs.graphs) {
                            if (graph.programId == program.id) {
                                graph.programName = program.name;
                                isEntryInDb = true;
                                break;
                            }
                        }
                        if (!isEntryInDb) {
                            dashboardGraphs.graphs.add(new DashboardGraphs.DashboardGraph
                                    (DashboardGraphs.GraphType.PROGRAM, true, program.id,
                                            program.name));
                        }
                    }

                    Iterator<DashboardGraphs.DashboardGraph> it = dashboardGraphs.graphs
                            .iterator();
                    while (it.hasNext()) {
                        DashboardGraphs.DashboardGraph graph = it.next();
                        if (graph.graphType == DashboardGraphs.GraphType.PROGRAM) {
                            boolean isStillValid = false;
                            for (Program program : programs) {
                                if (graph.programId == program.id) {
                                    isStillValid = true;
                                    break;
                                }
                            }
                            if (isStillValid) {
                                viewModel.programGraphs.add(graph);
                            } else {
                                // Remove from database
                                it.remove();
                            }
                        } else {
                            if (graph.graphType != DashboardGraphs.GraphType.DAILY_WATER_NEED ||
                                    features.hasDailyWaterNeedChart()) {
                                viewModel.generalGraphs.add(graph);
                            }
                        }
                    }
                    return viewModel;
                });
    }

    Observable<Irrelevant> saveToDatabase(DashboardGraphs dashboardGraphs) {
        return sprinklerRepository
                .saveDashboardGraphs(dashboardGraphs).toObservable()
                .compose(RunToCompletion.instance());
    }
}
