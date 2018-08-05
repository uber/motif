package testcases.E005_dependencies_cycle;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    @motif.Objects
    class Objects {

        @Named("a")
        String string(@Named("a") String a) {
            return a;
        }
    }
}
