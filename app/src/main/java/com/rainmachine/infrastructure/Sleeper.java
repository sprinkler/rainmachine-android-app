package com.rainmachine.infrastructure;

public class Sleeper {

    public static void sleepThrow(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public static void sleep(long millis) {
        try {
            sleepThrow(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
