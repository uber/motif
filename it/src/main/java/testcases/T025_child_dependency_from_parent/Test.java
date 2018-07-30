package testcases.T025_child_dependency_from_parent;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        Child child = scope.child();
        assertThat(child.string()).isEqualTo("cp");
    }
}
