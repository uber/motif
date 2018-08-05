package testcases.E006_dependencies_cycle_2;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    @motif.Objects
    class Objects {

        @Named("a")
        String a(@Named("b") String b) {
            return b;
        }

        @Named("b")
        String b(@Named("a") String a) {
            return a;
        }
    }
}
