package testcases.T013_override_scope;

@motif.Scope
public interface Scope extends ScopeParent {

    @Override
    String o();

    @motif.Objects
    class Objects {

        String string() {
            return "s";
        }
    }
}
