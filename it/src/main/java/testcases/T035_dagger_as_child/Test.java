package testcases.T035_dagger_as_child;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        Component component = DaggerComponent.builder()
                .module(new Component.Module())
                .parent(scope)
                .build();
        assertThat(component.string()).isEqualTo("motifdagger");
    }
}
