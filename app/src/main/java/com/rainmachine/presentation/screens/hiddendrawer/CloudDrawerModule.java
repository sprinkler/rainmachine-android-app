package com.rainmachine.presentation.screens.hiddendrawer;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.rainmachine.BuildConfig;
import com.rainmachine.R;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.CloudServers;
import com.rainmachine.data.local.pref.util.StringPreference;
import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.usecases.pushnotification.UpdatePushNotificationSettings;
import com.rainmachine.injection.Injector;
import com.rainmachine.presentation.activities.BaseActivity;
import com.rainmachine.presentation.screens.devices.DevicesActivity;
import com.rainmachine.presentation.util.RunOnProperThreads;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.palaima.debugdrawer.base.DebugModule;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class CloudDrawerModule implements DebugModule, AddCloudServersDialog.Callback,
        EditCloudServersDialog.Callback {

    @Inject
    DeviceRepository deviceRepository;
    @Inject
    UpdatePushNotificationSettings updatePushNotificationSettings;
    @Inject
    DatabaseRepositoryImpl databaseRepository;

    @Inject
    @Named("cloud_endpoint_pref")
    StringPreference cloudEndpointUrl;
    @Inject
    @Named("cloud_validate_endpoint_pref")
    StringPreference cloudValidateEndpointUrl;
    @Inject
    @Named("cloud_push_endpoint_pref")
    StringPreference cloudPushEndpointUrl;

    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.btn_edit)
    Button btnEdit;

    private BaseActivity activity;
    private List<CloudServers> items;
    private GenericSpinnerAdapter<CloudServers> adapter;

    public CloudDrawerModule(BaseActivity activity) {
        this.activity = activity;
        Injector.inject(this);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.hidden_drawer_module_cloud, parent, false);
        ButterKnife.bind(this, view);
        setup();
        return view;
    }

    private void setup() {
        adapter = new GenericSpinnerAdapter<>(activity, new ArrayList<>());
        spinner.setAdapter(adapter);

        databaseRepository.allCloudServers()
                .toObservable()
                .compose(RunOnProperThreads.instance())
                .subscribe(cloudServersList -> {
                    items = new ArrayList<>();
                    items.addAll(CloudDrawerModule.this.getFixedSpinnerList());
                    items.addAll(cloudServersList);
                    adapter.setItems(items);

                    int checkedItemPosition = getCheckedItemPosition();
                    btnEdit.setEnabled(checkedItemPosition >= 3);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int
                                position,
                                                   long id) {
                            CloudServers selected = adapter.getItem(position);
                            Timber.d("Selected %s %s %d", selected.key, selected.urlProxy,
                                    position);
                            btnEdit.setEnabled(position >= 3);
                            if (!selected.urlProxy.equals(cloudEndpointUrl.get())) {
                                updateUrlsUsedAndRestart(selected);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                    setSelection();
                });
    }

    private int getCheckedItemPosition() {
        int checkedItemPosition = 0;
        for (int i = 0; i < items.size(); i++) {
            if (cloudEndpointUrl.get().equals(items.get(i).urlProxy)) {
                checkedItemPosition = i;
            }
        }
        return checkedItemPosition;
    }

    private void updateUrlsUsedAndRestart(CloudServers cloudServers) {
        cloudEndpointUrl.set(cloudServers.urlProxy);
        cloudValidateEndpointUrl.set(cloudServers.urlValidator);
        cloudPushEndpointUrl.set(cloudServers.urlPush);
        Injector.createGraphAndInjectApp();
        Injector.inject(this);
        updatePushNotificationSettings.execute(new UpdatePushNotificationSettings.RequestModel())
                .subscribeOn(Schedulers.io())
                .subscribe();
        deviceRepository.deleteAllCloudDevices();
        // Restart app in order to do proper dependency injection
        activity.startActivity(DevicesActivity.getStartIntent(activity, true));
    }

    private void setSelection() {
        if (items != null) {
            spinner.setSelection(getCheckedItemPosition());
        }
    }

    @Override
    public void onOpened() {
        setSelection();
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

    @OnClick(R.id.btn_add)
    void onClickAdd() {
        DialogFragment dialog = AddCloudServersDialog.newInstance(this);
        activity.showDialogSafely(dialog);
    }

    @Override
    public void onDialogAddPositiveClick(CloudServers cloudServers) {
        databaseRepository.saveCloudServers(cloudServers)
                .flatMap(cloudServers12 -> databaseRepository.allCloudServers())
                .toObservable()
                .compose(RunOnProperThreads.instance())
                .subscribe(cloudServers1 -> {
                    items = new ArrayList<>();
                    items.addAll(getFixedSpinnerList());
                    items.addAll(cloudServers1);
                    adapter.setItems(items);
                });
    }

    @OnClick(R.id.btn_edit)
    void onClickEdit() {
        CloudServers item = (CloudServers) spinner.getSelectedItem();
        DialogFragment dialog = EditCloudServersDialog.newInstance(item, this);
        activity.showDialogSafely(dialog);
    }

    @Override
    public void onDialogEditPositiveClick(CloudServers cloudServers) {
        databaseRepository.saveCloudServers(cloudServers)
                .toObservable()
                .compose(RunOnProperThreads.instance())
                .subscribe(cloudServers1 -> updateUrlsUsedAndRestart(cloudServers1));
    }

    private List<CloudServers> getFixedSpinnerList() {
        List<CloudServers> list = new ArrayList<>();
        list.add(new CloudServers("staging",
                BuildConfig.CLOUD_SPRINKLERS_LIVE_URL, BuildConfig.CLOUD_VALIDATE_LIVE_URL,
                BuildConfig.CLOUD_PUSH_LIVE_URL));
        list.add(new CloudServers("dev", BuildConfig.CLOUD_SPRINKLERS_DEV_URL,
                BuildConfig.CLOUD_VALIDATE_DEV_URL, BuildConfig.CLOUD_PUSH_DEV_URL));
        list.add(new CloudServers("mini-box", BuildConfig.CLOUD_SPRINKLERS_MINI_BOX_URL,
                BuildConfig.CLOUD_VALIDATE_MINI_BOX_URL, BuildConfig.CLOUD_PUSH_MINI_BOX_URL));
        list.add(new CloudServers("new dev", BuildConfig.CLOUD_SPRINKLERS_NEW_DEV_URL,
                BuildConfig.CLOUD_VALIDATE_NEW_DEV_URL, BuildConfig.CLOUD_PUSH_NEW_DEV_URL));
        list.add(new CloudServers("test", BuildConfig.CLOUD_SPRINKLERS_TEST_URL,
                BuildConfig.CLOUD_VALIDATE_TEST_URL, BuildConfig.CLOUD_PUSH_TEST_URL));
        return list;
    }
}
