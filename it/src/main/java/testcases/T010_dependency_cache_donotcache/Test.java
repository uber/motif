package testcases.T010_dependency_cache_donotcache;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Scope scope = new ScopeImpl();
        assertThat(scope.string()).isEqualTo("a012");
    }
}
