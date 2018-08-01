package testcases.T002_access_method;

@motif.Scope
public interface Scope {

    String string();

    @motif.Objects
    class Objects {

        String string() {
            return "s";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
