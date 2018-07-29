package testcases.T011_inheritence_scope;

public interface ScopeParent<T, TT> extends ScopeGrandparent<TT> {

    T parent();
}
