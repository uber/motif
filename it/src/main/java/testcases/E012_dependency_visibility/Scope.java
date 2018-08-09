package testcases.E012_dependency_visibility;

@motif.Scope
public interface Scope {

    Child child();

    @motif.Objects
    class Objects {

        String string() {
            return "s";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
