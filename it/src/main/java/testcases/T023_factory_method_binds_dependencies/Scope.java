package testcases.T023_factory_method_binds_dependencies;

@motif.Scope
public interface Scope {

    B b();

    abstract class Objects {
        abstract A a();

        abstract B b(A a);

        abstract C c();
    }
}
