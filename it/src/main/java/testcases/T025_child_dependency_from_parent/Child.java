package testcases.T025_child_dependency_from_parent;

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
