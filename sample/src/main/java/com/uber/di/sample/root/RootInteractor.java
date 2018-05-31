package com.uber.di.sample.root;

import com.uber.di.samplelib.Stream;

public class RootInteractor {

    private final RootPresenter presenter;
    private final Stream stream;

    public RootInteractor(RootPresenter presenter, Stream stream) {
        this.presenter = presenter;
        this.stream = stream;
    }
}
