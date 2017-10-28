package com.rainmachine.presentation.screens.web;

import com.rainmachine.injection.SprinklerModule;

import dagger.Module;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                WebActivity.class
        }
)
class WebModule {
}
