package com.rainmachine.presentation.util;

public abstract class BasePresenter<V> implements Presenter<V> {

    protected V view;

    @Override
    public void attachView(V view) {
        this.view = view;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }
}
