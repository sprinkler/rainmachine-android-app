package com.rainmachine.presentation.screens.advancedsettings;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.handpreference.GetHandPreference;
import com.rainmachine.domain.usecases.handpreference.SaveHandPreference;
import com.rainmachine.domain.util.Features;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                AdvancedSettingsActivity.class,
                AdvancedSettingsView.class,
                RadioOptionsDialogFragment.class
        }
)
class AdvancedSettingsModule {

    private AdvancedSettingsActivity activity;

    AdvancedSettingsModule(AdvancedSettingsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    AdvancedSettingsActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    RadioOptionsDialogFragment.Callback provideRadioOptionsCallback(AdvancedSettingsPresenter
                                                                            presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    AdvancedSettingsPresenter providePresenter(AdvancedSettingsActivity activity,
                                               AdvancedSettingsMixer mixer, Features features) {
        return new AdvancedSettingsPresenter(activity, mixer, features);
    }

    @Provides
    @Singleton
    AdvancedSettingsMixer provideAdvancedSettingsMixer(SprinklerRepositoryImpl sprinklerRepository,
                                                       GetHandPreference getHandPreference,
                                                       SaveHandPreference saveHandPreference) {
        return new AdvancedSettingsMixer(sprinklerRepository, getHandPreference,
                saveHandPreference);
    }
}
