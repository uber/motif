package testcases.T018_spread_inheritence;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.a()).isEqualTo("a");
        assertThat(scope.b()).isEqualTo("b");
    }
}
