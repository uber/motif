package testcases.E015_missing_dependency_constructor;

@motif.Scope
public interface Scope {

    @motif.Objects
    abstract class Objects {
        abstract A a();
    }

    @motif.Dependencies
    interface Dependencies {}
}
