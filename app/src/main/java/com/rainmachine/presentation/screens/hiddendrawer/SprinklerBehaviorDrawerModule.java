package com.rainmachine.presentation.screens.hiddendrawer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.rainmachine.R;
import com.rainmachine.domain.util.Features;
import com.rainmachine.injection.Injector;
import com.rainmachine.presentation.screens.sprinklerdelegate.SprinklerDelegateMixer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.palaima.debugdrawer.base.DebugModule;

public class SprinklerBehaviorDrawerModule implements DebugModule {

    @BindView(R.id.spinner_wizard)
    Spinner spinnerWizard;
    @BindView(R.id.spinner_api)
    Spinner spinnerApi;

    private Activity activity;

    public SprinklerBehaviorDrawerModule(Activity activity) {
        this.activity = activity;
        Injector.inject(this);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.hidden_drawer_module_sprinkler_behavior, parent,
                false);
        ButterKnife.bind(this, view);
        setup();
        return view;
    }

    private void setup() {
        List<String> list = new ArrayList<>();
        list.add("default");
        list.add("wizard");
        GenericSpinnerAdapter<String> adapter = new GenericSpinnerAdapter<>(activity, list);
        spinnerWizard.setAdapter(adapter);
        int checkedItemPosition = SprinklerDelegateMixer.DEV_SPRINKLER_WIZARD ? 1 : 0;
        spinnerWizard.setSelection(checkedItemPosition);
        spinnerWizard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                       long id) {
                SprinklerDelegateMixer.DEV_SPRINKLER_WIZARD = position != 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        list = new ArrayList<>();
        list.add("Old API");
        list.add("New API");
        adapter = new GenericSpinnerAdapter<>(activity, list);
        spinnerApi.setAdapter(adapter);
        checkedItemPosition = Features.DEV_SPRINKLER_SPK1_OLD_API ? 0 : 1;
        spinnerApi.setSelection(checkedItemPosition);
        spinnerApi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                       long id) {
                Features.DEV_SPRINKLER_SPK1_OLD_API = position == 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void onOpened() {

    }

    @Override
    public void onClosed() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }
}
