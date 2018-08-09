package testcases.E003_scope_cycle;

import com.google.common.truth.Truth;
import motif.ir.graph.errors.GraphValidationErrors;
import motif.ir.graph.errors.ScopeCycleError;
import motif.ir.source.base.Type;

public class Test {

    public static GraphValidationErrors errors;

    public static void run() {
        ScopeCycleError error = errors.getScopeCycleError();
        Truth.assertThat(error).isNotNull();
        Truth.assertThat(error.getCycle())
                .containsExactly(new Type(null, Scope.class.getName()));
    }
}
