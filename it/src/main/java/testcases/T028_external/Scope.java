package testcases.T028_external;

import external.T028_external.Child;
import motif.Expose;

@motif.Scope
public interface Scope {

    Child child();

    @motif.Objects
    class Objects {

        @Expose
        String string() {
            return "s";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
