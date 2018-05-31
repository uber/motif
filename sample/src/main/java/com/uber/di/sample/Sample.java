package com.uber.di.sample;

import com.uber.di.sample.root.RootScopeImpl;
import com.uber.di.samplelib.game.GameRouter;

public class Sample {

    public static void main(String[] args) {
        GameRouter gameRouter = new RootScopeImpl(ViewGroup::new).getRouter().routeToLoggedIn("ASDF").routeToGameScope();
        System.out.println(gameRouter);
    }
}
