package testcases.E010_duplicate_factory_method_cross_module;

import external.E010_duplicate_factory_method_cross_module.Child;

@motif.Scope
public interface Scope {

    Child child();

    @motif.Objects
    class Objects {

        public String sa() {
            return "a";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
