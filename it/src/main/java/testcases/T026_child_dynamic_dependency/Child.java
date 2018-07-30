package testcases.T026_child_dynamic_dependency;

import motif.Scope;

import javax.inject.Named;

@Scope
public interface Child {

    @Named("c")
    String string();

    @motif.Objects
    class Objects {

        @Named("c")
        String string(@Named("p") String p) {
            return "c" + p;
        }
    }
}
