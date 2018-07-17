package com.uber.motif.sample;

import com.uber.motif.Scope;

@Scope
public interface RootScope {

    String a();

    abstract class Objects {

        String a() {
            return "HELLO WORLD!";
        }
    }
}
