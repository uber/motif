package testcases.T024_child;

@com.uber.motif.Scope
public interface Scope {

    String string();

    Child child();

    class Objects {

        String string() {
            return "p";
        }
    }
}
