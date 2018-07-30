package testcases.T015_factory_method_constructor;

@motif.Scope
public interface Scope {

    A a();

    abstract class Objects {
        abstract A a();
    }
}
