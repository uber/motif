package testcases.T017_spread;

import motif.Spread;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    @Named("a")
    String a();

    @Named("b")
    String b();

    @motif.Objects
    abstract class Objects {

        @Spread
        abstract Spreadable spreadable();
    }
}
