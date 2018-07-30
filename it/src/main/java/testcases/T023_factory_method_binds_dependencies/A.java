package testcases.T023_factory_method_binds_dependencies;

public class A extends B {

    private final C c;

    public A(C c) {
        this.c = c;
    }
}
