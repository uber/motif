package testcases.E002_nonstandard_dependencies_name;

import common.DependenciesSubject;
import motif.ir.graph.errors.GraphErrors;
import motif.ir.graph.errors.MissingDependenciesError;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        List<MissingDependenciesError> errors = Test.errors.getMissingDependenciesErrors();
        assertThat(errors).hasSize(1);
        DependenciesSubject.assertThat(errors.get(0).getDependencies())
                .with(String.class, Scope.class)
                .matches();
    }
}
