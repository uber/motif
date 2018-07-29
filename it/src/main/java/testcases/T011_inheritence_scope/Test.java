package testcases.T011_inheritence_scope;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.parent()).isEqualTo("a");
        assertThat(scope.grandparent()).isEqualTo(1);
    }
}
