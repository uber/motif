package testcases.E011_dynamic_dependency_internal;

import common.DependenciesSubject;
import motif.ir.graph.errors.GraphErrors;
import motif.ir.graph.errors.MissingDependenciesError;
import motif.ir.source.dependencies.Dependencies;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        List<MissingDependenciesError> errors = Test.errors.getMissingDependenciesErrors();
        assertThat(errors).hasSize(1);
        Dependencies dependencies = errors.get(0).getDependencies();
        DependenciesSubject.assertThat(dependencies)
                .with(String.class, Grandchild.class)
                .matches();
    }
}
