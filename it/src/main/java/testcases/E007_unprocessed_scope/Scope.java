package testcases.E007_unprocessed_scope;

import external.E007_unprocessed_scope.Child;

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
