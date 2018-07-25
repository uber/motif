package com.uber.motif;

import org.junit.Test;

import javax.inject.Named;

public class ParentNameCollisionTest {

    @Test
    public void collision() { }

    @Scope
    interface Parent {

        ChildA a();
        ChildB b();
        ChildC c();
        ChildD d();
    }

    @Scope
    interface ChildA {
        @Named("A")
        String string();
    }

    @Scope
    interface ChildB {
        @Named("B")
        String string();
    }

    @Scope
    interface ChildC {

        a.SomeDependency a();
    }

    @Scope
    interface ChildD {

        b.SomeDependency b();
    }
}
