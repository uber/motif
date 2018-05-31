package com.uber.di.samplelib.loggedin;

import com.uber.di.samplelib.Router;
import com.uber.di.samplelib.game.GameRouter;

public class LoggedInRouter extends Router {

    private final LoggedInScope scope;
    private final LoggedInInteractor inInteractor;

    public LoggedInRouter(LoggedInScope scope, LoggedInInteractor inInteractor) {
        this.scope = scope;
        this.inInteractor = inInteractor;
    }

    public GameRouter routeToGameScope() {
        return scope.gameScope().getRouter();
    }
}
