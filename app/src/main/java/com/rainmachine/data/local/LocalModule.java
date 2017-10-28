package com.rainmachine.data.local;

import com.rainmachine.data.local.database.DatabaseModule;
import com.rainmachine.data.local.pref.PrefModule;

import dagger.Module;

@Module(
        includes = {
                PrefModule.class,
                DatabaseModule.class
        },
        complete = false,
        library = true
)
public class LocalModule {
}
