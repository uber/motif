package testcases.T022_factory_method_constructor_dependencies;

@com.uber.motif.Scope
public interface Scope {

    B b();

    abstract class Objects {
        abstract A a();
        abstract B b();
    }
}
