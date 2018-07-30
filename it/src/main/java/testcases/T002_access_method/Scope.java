package testcases.T002_access_method;

@motif.Scope
public interface Scope {

    String string();

    class Objects {

        String string() {
            return "s";
        }
    }
}
