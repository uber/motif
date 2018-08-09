package testcases.E011_dynamic_dependency_internal;

@motif.Scope
public interface Scope {

    Child child(String string);

    @motif.Dependencies
    interface Dependencies {}
}
