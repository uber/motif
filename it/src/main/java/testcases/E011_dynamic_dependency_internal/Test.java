package testcases.E011_dynamic_dependency_internal;

import common.MissingDependenciesSubject;
import motif.ir.graph.errors.GraphValidationErrors;
import motif.ir.graph.errors.MissingDependenciesError;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static GraphValidationErrors errors;

    public static void run() {
        List<MissingDependenciesError> errors = Test.errors.getMissingDependenciesErrors();
        assertThat(errors).hasSize(1);
        MissingDependenciesSubject.assertThat(errors.get(0))
                .matches(Grandchild.class, String.class);
    }
}
