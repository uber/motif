package testcases.T016_factory_method_binds;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.b()).isInstanceOf(A.class);
    }
}
