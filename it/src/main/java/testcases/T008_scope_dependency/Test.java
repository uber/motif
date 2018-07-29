package testcases.T008_scope_dependency;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.string()).isEqualTo("sa");
    }
}
