package testcases.T011_inheritence_scope;

@motif.Scope
public interface Scope extends ScopeParent<String, Integer> {

    @motif.Objects
    class Objects {

        String string() {
            return "a";
        }

        Integer integer() {
            return 1;
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
