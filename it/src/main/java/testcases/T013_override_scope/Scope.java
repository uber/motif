package testcases.T013_override_scope;

@motif.Scope
public interface Scope extends ScopeParent {

    @Override
    String o();

    class Objects {

        String string() {
            return "s";
        }
    }
}
