package testcases.T028_external;

import external.T028_external.Child;

@motif.Scope
public interface Scope {

    Child child();

    @motif.Objects
    class Objects {

        public String string() {
            return "s";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
