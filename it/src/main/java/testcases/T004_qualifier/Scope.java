package testcases.T004_qualifier;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    String string();

    class Objects {

        String s(@Named("a") String a, @Named("b") String b) {
            return a + b;
        }

        @Named("a")
        String a() {
            return "a";
        }

        @Named("b")
        String b() {
            return "b";
        }
    }
}
