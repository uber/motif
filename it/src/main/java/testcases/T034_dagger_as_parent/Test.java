package testcases.T034_dagger_as_parent;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Component component = DaggerComponent.create();
        Scope scope = new ScopeImpl(component);
        assertThat(scope.string()).isEqualTo("motifdagger");
    }
}
