package testcases.T015_factory_method_constructor;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.a()).isInstanceOf(A.class);
    }
}
