package testcases.T027_nonstandard_objects_name;

@motif.Scope
public interface Scope {

    String string();

    @motif.Objects
    class MyObjects {

        String string() {
            return "s";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
