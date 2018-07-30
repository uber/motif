package testcases.T022_factory_method_constructor_dependencies;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.b()).isNotNull();
    }
}
