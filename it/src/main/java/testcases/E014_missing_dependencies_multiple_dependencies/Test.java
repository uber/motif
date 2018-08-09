package testcases.E014_missing_dependencies_multiple_dependencies;

import com.google.common.truth.Truth;
import common.MissingDependenciesSubject;
import motif.ir.graph.errors.GraphValidationErrors;
import motif.ir.graph.errors.MissingDependenciesError;

import java.util.List;

public class Test {

    public static GraphValidationErrors errors;

    public static void run() {
        List<MissingDependenciesError> errors = Test.errors.getMissingDependenciesErrors();
        Truth.assertThat(errors).hasSize(1);
        MissingDependenciesError error = errors.get(0);
        MissingDependenciesSubject.assertThat(error)
                .matches(Scope.class, String.class, Integer.class);
    }
}
