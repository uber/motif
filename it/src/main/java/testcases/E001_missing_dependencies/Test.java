package testcases.E001_missing_dependencies;

import com.google.common.truth.Truth;
import common.DependenciesSubject;
import motif.ir.graph.errors.GraphErrors;
import motif.ir.graph.errors.MissingDependenciesError;

import java.util.List;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        List<MissingDependenciesError> errors = Test.errors.getMissingDependenciesErrors();
        Truth.assertThat(errors).hasSize(1);
        DependenciesSubject.assertThat(errors.get(0).getDependencies())
                .with(String.class, Scope.class)
                .matches();
    }
}
