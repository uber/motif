package testcases.E016_missing_dependencies_multilevel;

@motif.Scope
public interface Scope {

    Child child();

    @motif.Dependencies
    interface Dependencies {}
}
