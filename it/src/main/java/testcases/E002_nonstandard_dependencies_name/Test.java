package testcases.E002_nonstandard_dependencies_name;

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
