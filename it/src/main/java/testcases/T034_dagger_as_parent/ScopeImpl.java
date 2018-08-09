package testcases.T034_dagger_as_parent;

public class ScopeImpl implements Scope {

    public ScopeImpl(Dependencies dependencies) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String string() {
        throw new UnsupportedOperationException();
    }

    interface Dependencies {}
}
