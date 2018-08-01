package testcases.T014_override_objects;

@motif.Scope
public interface Scope {

    A a();

    @motif.Objects
    abstract class Objects extends ObjectsParent {

        abstract A b();
    }

    @motif.Dependencies
    interface Dependencies {}
}
