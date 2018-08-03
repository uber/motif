package testcases.T028_external;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.child().string()).isEqualTo("s");
    }
}
