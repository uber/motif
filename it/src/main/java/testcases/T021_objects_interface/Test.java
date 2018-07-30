package testcases.T021_objects_interface;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.dependency()).isNotNull();
    }
}
