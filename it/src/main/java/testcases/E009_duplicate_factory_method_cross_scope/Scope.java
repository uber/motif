package testcases.E009_duplicate_factory_method_cross_scope;

import motif.Expose;

@motif.Scope
public interface Scope {

    Child child();

    @motif.Objects
    class Objects {

        @Expose
        String sa() {
            return "a";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
