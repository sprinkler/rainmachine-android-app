package com.rainmachine.presentation.screens.mini8settings;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.InputNumberDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                Mini8SettingsActivity.class,
                Mini8SettingsView.class,
                InputNumberDialogFragment.class,
                Mini8SettingsProgramsDialogFragment.class
        }
)
class Mini8SettingsModule {

    private Mini8SettingsContract.Container container;

    Mini8SettingsModule(Mini8SettingsContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    InputNumberDialogFragment.Callback provideInputCallback(Mini8SettingsContract.Presenter
                                                                    presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    Mini8SettingsContract.Presenter providePresenter(Mini8SettingsMixer mixer) {
        return new Mini8SettingsPresenter(container, mixer);
    }

    @Provides
    @Singleton
    Mini8SettingsMixer provideMini8SettingsMixer(SprinklerRepositoryImpl sprinklerRepository) {
        return new Mini8SettingsMixer(sprinklerRepository);
    }
}
