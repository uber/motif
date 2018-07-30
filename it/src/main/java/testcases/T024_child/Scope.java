package testcases.T024_child;

@motif.Scope
public interface Scope {

    String string();

    Child child();

    @motif.Objects
    class Objects {

        String string() {
            return "p";
        }
    }
}
