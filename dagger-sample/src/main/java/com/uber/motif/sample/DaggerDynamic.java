package com.uber.motif.sample;

import com.uber.motif.Scope;

import javax.inject.Named;

@Scope
interface MotifScope {

    class Objects {

        @Named("A")
        String a() {
            return "A";
        }
    }
}