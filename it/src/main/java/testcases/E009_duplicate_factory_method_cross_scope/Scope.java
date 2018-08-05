package testcases.E009_duplicate_factory_method_cross_scope;

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
