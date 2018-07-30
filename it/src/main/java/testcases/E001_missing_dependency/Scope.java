package testcases.E001_missing_dependency;

@motif.Scope
public interface Scope {

    String string();

    @motif.Dependencies
    interface Dependencies {}
}
