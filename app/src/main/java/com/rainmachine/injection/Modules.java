package com.rainmachine.injection;

import com.rainmachine.infrastructure.util.RainApplication;

public final class Modules {
    public static Object[] list(RainApplication app) {
        return new Object[]{
                new AppModule(app),
                new HiddenDrawerModule()
        };
    }

    private Modules() {
        // No instances.
    }
}
