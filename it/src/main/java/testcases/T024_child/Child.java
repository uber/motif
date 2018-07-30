package testcases.T024_child;

@com.uber.motif.Scope
public interface Child {

    String string();

    class Objects {

        String string() {
            return "c";
        }
    }
}
