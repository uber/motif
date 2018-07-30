package testcases.T024_child;

import motif.Scope;

@Scope
public interface Child {

    String string();

    class Objects {

        String string() {
            return "c";
        }
    }
}
