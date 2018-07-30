package testcases.T012_inheritence_objects;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.parent()).isEqualTo("parent");
        assertThat(scope.grandparent()).isEqualTo("grandparent");
        assertThat(scope.a()).isNotNull();
        assertThat(scope.b()).isNotNull();
    }
}
