package testcases.E013_missing_dependencies_multiple_scopes;

@motif.Scope
public interface Scope {

    ChildA a();
    ChildB b();

    @motif.Dependencies
    interface Dependencies {}
}
