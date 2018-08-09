package testcases.E001_missing_dependencies;

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
        MissingDependenciesSubject.assertThat(errors.get(0))
                .matches(Scope.class, String.class);
    }
}
