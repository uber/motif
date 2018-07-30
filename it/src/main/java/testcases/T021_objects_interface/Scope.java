package testcases.T021_objects_interface;

@motif.Scope
public interface Scope {

    Dependency dependency();

    interface Objects {

        Dependency dependency();
    }
}
