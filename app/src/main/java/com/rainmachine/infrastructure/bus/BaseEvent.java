package com.rainmachine.infrastructure.bus;

public class BaseEvent {
    public static final int EVENT_TYPE_SUCCESS = 0;
    public static final int EVENT_TYPE_PROGRESS = 1;
    public static final int EVENT_TYPE_ERROR = 2;

    public int type;

    public BaseEvent() {
    }

    public BaseEvent(int type) {
        this.type = type;
    }
}
