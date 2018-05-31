package com.uber.di.samplelib.game;

import com.uber.motif.Scope;
import com.uber.di.samplelib.Stream;

@Scope
public interface GameScope {

    GameRouter getRouter();

    class Objects {
        GameRouter router(Stream stream) {
            return new GameRouter(stream);
        }
    }
}
