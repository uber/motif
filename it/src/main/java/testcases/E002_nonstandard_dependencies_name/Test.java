package testcases.E002_nonstandard_dependencies_name;

import common.DependenciesSubject;
import motif.ir.graph.GraphErrors;
import motif.ir.graph.errors.MissingDependenciesError;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        MissingDependenciesError error = errors.getMissingDependenciesError();
        assertThat(error).isNotNull();
        DependenciesSubject.assertThat(error.getDependencies())
                .with(String.class, Scope.class)
                .matches();
    }
}
