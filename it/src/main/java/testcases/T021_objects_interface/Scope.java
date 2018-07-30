package testcases.T021_objects_interface;

@com.uber.motif.Scope
public interface Scope {

    Dependency dependency();

    interface Objects {

        Dependency dependency();
    }
}
