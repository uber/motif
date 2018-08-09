package testcases.E014_missing_dependencies_multiple_dependencies;

@motif.Scope
public interface Scope {

    String string();

    Integer integer();

    @motif.Dependencies
    interface Dependencies {}
}
