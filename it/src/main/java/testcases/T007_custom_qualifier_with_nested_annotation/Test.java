package testcases.T007_custom_qualifier_with_nested_annotation;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.string()).isEqualTo("ab");
    }
}
