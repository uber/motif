package testcases.T033_unprocessed_scope;

import external.T033_unprocessed_scope.Child;
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
