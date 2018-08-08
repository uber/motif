package testcases.T025_child_dependency_from_parent;

import motif.Expose;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    Child child();

    @motif.Objects
    class Objects {

        @Expose
        @Named("p")
        String string() {
            return "p";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
