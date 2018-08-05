package testcases.E001_missing_dependencies;

import com.google.common.truth.Truth;
import common.DependenciesSubject;
import motif.ir.graph.GraphErrors;
import motif.ir.graph.errors.MissingDependenciesError;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        MissingDependenciesError error = errors.getMissingDependenciesError();
        Truth.assertThat(error).isNotNull();
        DependenciesSubject.assertThat(error.getDependencies())
                .with(String.class, Scope.class)
                .matches();
    }
}
