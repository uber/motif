package testcases.E006_dependencies_cycle_2;

import com.google.common.truth.Truth;
import common.DependencyCycleSubject;
import motif.ir.graph.errors.DependencyCycleError;
import motif.ir.graph.errors.GraphValidationErrors;

public class Test {

    public static GraphValidationErrors errors;

    public static void run() {
        DependencyCycleError error = errors.getDependencyCycleError();
        Truth.assertThat(error).isNotNull();
        DependencyCycleSubject.assertThat(error)
                .with(Scope.class, "a", "b")
                .matches();
    }
}
