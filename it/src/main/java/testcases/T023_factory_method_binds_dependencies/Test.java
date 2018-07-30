package testcases.T023_factory_method_binds_dependencies;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.b()).isInstanceOf(A.class);
    }
}
