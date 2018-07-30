package testcases.T024_child;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.string()).isEqualTo("p");

        Child child = scope.child();
        assertThat(child.string()).isEqualTo("c");

        assertThat(scope.child()).isNotSameAs(child);
    }
}
