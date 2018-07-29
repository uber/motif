package testcases.T017_spread;

import com.uber.motif.Spread;

import javax.inject.Named;

@com.uber.motif.Scope
public interface Scope {

    @Named("a")
    String a();

    @Named("b")
    String b();

    abstract class Objects {

        @Spread
        abstract Spreadable spreadable();
    }
}
