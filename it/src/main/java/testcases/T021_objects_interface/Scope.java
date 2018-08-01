package testcases.T021_objects_interface;

@motif.Scope
public interface Scope {

    Dependency dependency();

    @motif.Objects
    interface Objects {

        Dependency dependency();
    }

    @motif.Dependencies
    interface Dependencies {}
}
