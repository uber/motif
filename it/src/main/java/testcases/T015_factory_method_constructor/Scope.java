package testcases.T015_factory_method_constructor;

@motif.Scope
public interface Scope {

    A a();

    @motif.Objects
    abstract class Objects {
        abstract A a();
    }

    @motif.Dependencies
    interface Dependencies {}
}
