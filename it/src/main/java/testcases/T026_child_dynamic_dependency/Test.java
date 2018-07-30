package testcases.T026_child_dynamic_dependency;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        Child child = scope.child("p");
        assertThat(child.string()).isEqualTo("cp");
    }
}
