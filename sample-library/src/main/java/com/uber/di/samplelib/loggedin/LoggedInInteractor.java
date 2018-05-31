package com.uber.di.samplelib.loggedin;

import com.uber.di.samplelib.Stream;

public class LoggedInInteractor {

    private final Stream stream;
    private final String authToken;

    public LoggedInInteractor(Stream stream, String authToken) {
        this.stream = stream;
        this.authToken = authToken;
    }
}
