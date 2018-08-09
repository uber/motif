package testcases.E013_missing_dependencies_multiple_scopes;

import com.google.common.truth.Truth;
import common.MissingDependenciesSubject;
import motif.ir.graph.errors.GraphErrors;
import motif.ir.graph.errors.MissingDependenciesError;

import java.util.List;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        List<MissingDependenciesError> errors = Test.errors.getMissingDependenciesErrors();
        Truth.assertThat(errors).hasSize(2);
        MissingDependenciesSubject.assertThat(errors.get(0))
                .matches(ChildA.class, String.class);
        MissingDependenciesSubject.assertThat(errors.get(1))
                .matches(ChildB.class, String.class);
    }
}
