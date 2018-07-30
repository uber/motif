package testcases.T025_child_dependency_from_parent;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    Child child();

    @motif.Objects
    class Objects {

        @Named("p")
        public String string() {
            return "p";
        }
    }
}
