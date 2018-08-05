package testcases.E005_dependencies_cycle;

import com.google.common.truth.Truth;
import motif.ir.graph.errors.GraphErrors;
import motif.ir.graph.errors.ScopeCycleError;
import motif.ir.source.base.Type;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        ScopeCycleError error = errors.getScopeCycleError();
        Truth.assertThat(error).isNotNull();
        Truth.assertThat(error.getCycle())
                .containsExactly(new Type(null, Scope.class.getName()));
    }
}
