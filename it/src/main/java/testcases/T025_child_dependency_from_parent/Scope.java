package testcases.T025_child_dependency_from_parent;

import javax.inject.Named;

@com.uber.motif.Scope
public interface Scope {

    Child child();

    class Objects {

        @Named("p")
        public String string() {
            return "p";
        }
    }
}
