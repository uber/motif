package testcases.T014_override_objects;

@com.uber.motif.Scope
public interface Scope {

    A a();

    abstract class Objects extends ObjectsParent {

        abstract A b();
    }
}
