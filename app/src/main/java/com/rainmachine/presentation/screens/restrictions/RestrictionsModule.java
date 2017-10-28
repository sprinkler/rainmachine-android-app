package com.rainmachine.presentation.screens.restrictions;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.InputNumberNoteDialogFragment;
import com.rainmachine.presentation.dialogs.MultiChoiceDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                RestrictionsActivity.class,
                RestrictionsView.class,
                FreezeProtectDialogFragment.class,
                MultiChoiceDialogFragment.class,
                RestrictedMonthsDialogFragment.class,
                RestrictedDaysDialogFragment.class,
                InputNumberNoteDialogFragment.class,
                HotDaysDialogFragment.class
        }
)
class RestrictionsModule {

    private RestrictionsActivity activity;

    RestrictionsModule(RestrictionsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    RestrictionsActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    FreezeProtectDialogFragment.Callback provideFreezeProtectDialogCallback(RestrictionsPresenter
                                                                                    presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    RestrictedDaysDialogFragment.Callback provideDaysDialogCallback(RestrictionsPresenter
                                                                            presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    RestrictedMonthsDialogFragment.Callback provideMonthsDialogCallback(RestrictionsPresenter
                                                                                presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    InputNumberNoteDialogFragment.Callback provideInputNumberNoteCallback(RestrictionsPresenter
                                                                                  presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    RestrictionsPresenter providePresenter(RestrictionsActivity activity,
                                           RestrictionsMixer mixer) {
        return new RestrictionsPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    RestrictionsMixer provideRestrictionsMixer(SprinklerRepositoryImpl sprinklerRepository,
                                               GetRestrictionsLive getRestrictionsLive) {
        return new RestrictionsMixer(sprinklerRepository, getRestrictionsLive);
    }
}
