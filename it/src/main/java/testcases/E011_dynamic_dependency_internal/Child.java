package testcases.E011_dynamic_dependency_internal;

@motif.Scope
public interface Child {

    Grandchild grandchild();

    String string();
}
