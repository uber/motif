package external.E010_duplicate_factory_method_cross_module;

@motif.Scope
public interface Child {

    @motif.Objects
    class Objects {

        String sb() {
            return "b";
        }
    }
}
