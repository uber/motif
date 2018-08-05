package testcases.E008_duplicate_factory_method;

@motif.Scope
public interface Scope {

    @motif.Objects
    class Objects {

        String sa() {
            return "a";
        }

        String sb() {
            return "b";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
