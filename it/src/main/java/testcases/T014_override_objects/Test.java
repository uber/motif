package testcases.T014_override_objects;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.a()).isInstanceOf(A.class);
    }
}
