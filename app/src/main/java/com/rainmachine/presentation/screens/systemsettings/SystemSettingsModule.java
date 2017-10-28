package com.rainmachine.presentation.screens.systemsettings;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.notifiers.DeviceNameStore;
import com.rainmachine.domain.notifiers.StatsNeedRefreshNotifier;
import com.rainmachine.domain.usecases.pushnotification.UpdatePushNotificationSettings;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.infrastructure.SprinklerUtils;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.DatePickerDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.dialogs.ItemsDialogFragment;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;
import com.rainmachine.presentation.screens.wizardtimezone.TimezoneDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                SystemSettingsActivity.class,
                SystemSettingsView.class,
                DeviceNameDialogFragment.class,
                DatePickerDialogFragment.class,
                TimePickerDialogFragment.class,
                RadioOptionsDialogFragment.class,
                ItemsDialogFragment.class,
                ActionMessageDialogFragment.class,
                TimezoneDialogFragment.class,
                InfoMessageDialogFragment.class
        }
)
class SystemSettingsModule {

    private SystemSettingsActivity activity;

    SystemSettingsModule(SystemSettingsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    SystemSettingsActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    DatePickerDialogFragment.Callback provideDatePickerCallback(SystemSettingsPresenter
                                                                        systemSettingsPresenter) {
        return systemSettingsPresenter;
    }

    @Provides
    @Singleton
    TimePickerDialogFragment.Callback provideTimePickerCallback(SystemSettingsPresenter
                                                                        systemSettingsPresenter) {
        return systemSettingsPresenter;
    }

    @Provides
    @Singleton
    RadioOptionsDialogFragment.Callback provideRadioOptionsDialogCallback(SystemSettingsPresenter
                                                                                  systemSettingsPresenter) {
        return systemSettingsPresenter;
    }

    @Provides
    @Singleton
    TimezoneDialogFragment.Callback provideTimezoneDialogCallback(SystemSettingsPresenter
                                                                          systemSettingsPresenter) {
        return systemSettingsPresenter;
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback
            (SystemSettingsPresenter systemSettingsPresenter) {
        return systemSettingsPresenter;
    }

    @Provides
    @Singleton
    InfoMessageDialogFragment.Callback provideInfoMessageDialogCallback
            (SystemSettingsPresenter systemSettingsPresenter) {
        return systemSettingsPresenter;
    }

    @Provides
    @Singleton
    SystemSettingsPresenter providePresenter(SystemSettingsActivity activity, Features features,
                                             SystemSettingsMixer mixer,
                                             Device device, DeviceNameStore deviceNameStore) {
        return new SystemSettingsPresenter(activity, features, mixer, device, deviceNameStore);
    }

    @Provides
    @Singleton
    SystemSettingsMixer provideDeviceSettingsMixer(Device device,
                                                   SprinklerUtils sprinklerUtils,
                                                   SprinklerState sprinklerState,
                                                   Features features,
                                                   DatabaseRepositoryImpl databaseRepository,
                                                   StatsNeedRefreshNotifier
                                                           statsNeedRefreshNotifier,
                                                   SprinklerRepositoryImpl sprinklerRepository,
                                                   DeviceNameStore deviceNameStore,
                                                   UpdatePushNotificationSettings
                                                           updatePushNotificationSettings) {
        return new SystemSettingsMixer(device, sprinklerUtils, sprinklerState, features,
                databaseRepository, statsNeedRefreshNotifier, sprinklerRepository,
                deviceNameStore, updatePushNotificationSettings);
    }
}
