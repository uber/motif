package testcases.E001_missing_dependencies;

import motif.ir.graph.GraphErrors;

import static common.DependenciesSubject.assertThat;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        assertThat(errors.getMissingDependencies())
                .with(String.class, Scope.class)
                .matches();
    }
}
