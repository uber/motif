package testcases.E009_duplicate_factory_method_cross_scope;

@motif.Scope
public interface Child {

    @motif.Objects
    class Objects {

        String sb() {
            return "b";
        }
    }
}
