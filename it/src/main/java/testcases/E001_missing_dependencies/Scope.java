package testcases.E001_missing_dependencies;

@motif.Scope
public interface Scope {

    String string();

    @motif.Objects
    class Objects {}

    @motif.Dependencies
    interface Dependencies {}
}
