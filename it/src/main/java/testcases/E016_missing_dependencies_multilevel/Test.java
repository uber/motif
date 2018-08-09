package testcases.E016_missing_dependencies_multilevel;

import common.MissingDependenciesSubject;
import motif.ir.graph.errors.GraphValidationErrors;
import motif.ir.graph.errors.MissingDependenciesError;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static GraphValidationErrors errors;

    public static void run() {
        List<MissingDependenciesError> errors = Test.errors.getMissingDependenciesErrors();
        assertThat(errors).hasSize(2);
        MissingDependenciesSubject.assertThat(errors.get(0))
                .matches(Grandchild.class, Integer.class);
        MissingDependenciesSubject.assertThat(errors.get(1))
                .matches(Child.class, String.class);
    }
}
