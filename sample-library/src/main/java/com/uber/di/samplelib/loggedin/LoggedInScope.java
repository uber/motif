package com.uber.di.samplelib.loggedin;

import com.uber.motif.Scope;
import com.uber.di.samplelib.game.GameScope;

@Scope
public interface LoggedInScope {

    LoggedInRouter getRouter();

    GameScope gameScope();

    abstract class Objects {

        abstract LoggedInInteractor interactor();

        LoggedInRouter router(LoggedInScope scope, LoggedInInteractor interactor) {
            return new LoggedInRouter(scope, interactor);
        }
    }
}
