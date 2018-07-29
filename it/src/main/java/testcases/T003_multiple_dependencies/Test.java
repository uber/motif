package testcases.T003_multiple_dependencies;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.string()).isEqualTo("s:1");
    }
}
