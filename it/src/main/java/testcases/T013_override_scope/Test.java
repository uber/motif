package testcases.T013_override_scope;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.o()).isEqualTo("s");
    }
}
