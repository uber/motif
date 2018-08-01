package testcases.E001_nonstandard_dependencies_name;

import motif.compiler.errors.MissingDependenciesError;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static MissingDependenciesError expectedError;

    public static void run() {
        assertThat(expectedError).isNotNull();
        assertThat(expectedError.getMissingDependencies().getList().size()).isEqualTo(1);
    }
}
