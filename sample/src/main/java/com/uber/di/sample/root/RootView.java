package com.uber.di.sample.root;

import com.uber.di.sample.ViewGroup;

public class RootView implements RootPresenter {

    public static RootView inflate(ViewGroup parent) {
        return new RootView();
    }
}
