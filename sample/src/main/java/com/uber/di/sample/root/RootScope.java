package com.uber.di.sample.root;

import com.uber.motif.Scope;
import com.uber.di.sample.ViewGroup;
import com.uber.di.sample.loggedout.LoggedOutScope;
import com.uber.di.samplelib.Stream;
import com.uber.di.samplelib.loggedin.LoggedInScope;

@Scope
public interface RootScope {

    RootRouter getRouter();

    LoggedInScope loggedIn(String authToken);
    LoggedOutScope loggedOut();
    RootScope root(ViewGroup parent);

    abstract class Objects {

        public abstract Stream stream();

        abstract com.uber.di.samplelib.other.Stream otherStream();
        abstract RootRouter router();
        abstract RootInteractor interactor();
        abstract RootPresenter presenter(RootView view);

        RootView view(ViewGroup parent) {
            return RootView.inflate(parent);
        }
    }

    interface Parent {

        ViewGroup parent();
    }
}
