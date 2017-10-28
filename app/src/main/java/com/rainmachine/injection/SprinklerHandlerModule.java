package com.rainmachine.injection;

import android.content.Context;

import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.infrastructure.UpdateHandler;
import com.rainmachine.presentation.util.ForegroundDetector;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(complete = false, library = true)
class SprinklerHandlerModule {

    @Provides
    @Singleton
    UpdateHandler provideUpdateHandler(Context context, Device device, Features features,
                                       SprinklerState sprinklerState,
                                       SprinklerRepository sprinklerRepository,
                                       ForegroundDetector foregroundDetector) {
        return new UpdateHandler(context, device, features, sprinklerState, sprinklerRepository,
                foregroundDetector);
    }
}
