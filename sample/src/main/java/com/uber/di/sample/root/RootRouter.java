package com.uber.di.sample.root;

import com.uber.di.samplelib.Router;
import com.uber.di.samplelib.loggedin.LoggedInRouter;

public class RootRouter extends Router {

    private final RootScope scope;
    private final RootInteractor interactor;

    public RootRouter(RootScope scope, RootInteractor interactor) {
        this.scope = scope;
        this.interactor = interactor;
    }

    public LoggedInRouter routeToLoggedIn(String token) {
        return scope.loggedIn(token).getRouter();
    }
}
