package testcases.E001_missing_dependencies;

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
