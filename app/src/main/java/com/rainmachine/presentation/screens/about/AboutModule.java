package com.rainmachine.presentation.screens.about;

import android.content.Context;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.TriggerUpdateCheck;
import com.rainmachine.domain.util.Features;
import com.rainmachine.infrastructure.UpdateHandler;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                AboutActivity.class,
                AboutView.class,
                ActionMessageDialogFragment.class
        }
)
class AboutModule {

    private AboutActivity activity;

    AboutModule(AboutActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    AboutActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback(AboutContract.Presenter
                                                                                    presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    AboutContract.Presenter providePresenter(AboutActivity activity, AboutMixer mixer,
                                             Features features, UpdateHandler updateHandler) {
        return new AboutPresenter(activity, mixer, features, updateHandler);
    }

    @Provides
    @Singleton
    AboutMixer provideAboutMixer(Context context, Features features, UpdateHandler updateHandler,
                                 SprinklerRepositoryImpl sprinklerRepository, CalendarFormatter
                                         formatter, TriggerUpdateCheck triggerUpdateCheck) {
        return new AboutMixer(context, features, updateHandler, triggerUpdateCheck,
                sprinklerRepository, formatter);
    }
}
