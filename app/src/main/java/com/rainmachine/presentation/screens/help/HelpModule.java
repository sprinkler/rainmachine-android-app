package com.rainmachine.presentation.screens.help;

import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                HelpActivity.class
        }
)
class HelpModule {

    private HelpContract.View view;

    HelpModule(HelpContract.View view) {
        this.view = view;
    }

    @Provides
    @Singleton
    HelpContract.Presenter providePresenter() {
        return new HelpPresenter(view);
    }
}
