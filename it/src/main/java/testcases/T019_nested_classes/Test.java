package testcases.T019_nested_classes;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new Test_ScopeImpl();
        assertThat(scope.string()).isEqualTo("s");
    }

    @motif.Scope
    public interface Scope {

        String string();

        @motif.Objects
        abstract class Objects {

            String string() {
                return "s";
            }
        }

        @motif.Dependencies
        interface Dependencies {}
    }
}
