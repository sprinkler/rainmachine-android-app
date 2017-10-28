package com.rainmachine.presentation.util;

public interface Presenter<V> {

    void attachView(V view);

    void init();

    void destroy();
}
