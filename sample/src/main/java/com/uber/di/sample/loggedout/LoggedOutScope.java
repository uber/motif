package com.uber.di.sample.loggedout;

import com.uber.motif.Scope;
import com.uber.di.samplelib.Stream;

@Scope
public interface LoggedOutScope {

    LoggedOutInteractor interactor();

    class Objects {

        LoggedOutInteractor interactor(Stream stream) {
            return new LoggedOutInteractor();
        }
    }
}
