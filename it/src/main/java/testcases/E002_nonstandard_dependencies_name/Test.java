package testcases.E002_nonstandard_dependencies_name;

import motif.compiler.errors.MissingDependenciesError;

import static common.DependenciesSubject.assertThat;

public class Test {

    public static MissingDependenciesError expectedError;

    public static void run() {
        assertThat(expectedError.getMissingDependencies())
                .with(String.class, Scope.class)
                .matches();
    }
}
