package testcases.E004_scope_cycle_2;

import com.google.common.truth.Truth;
import motif.ir.graph.GraphErrors;
import motif.ir.graph.errors.ScopeCycleError;
import motif.ir.source.base.Type;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        ScopeCycleError error = errors.getScopeCycleError();
        Truth.assertThat(error).isNotNull();
        Truth.assertThat(error.getCycle())
                .containsExactly(
                        new Type(null, Scope.class.getName()),
                        new Type(null, Child.class.getName()));
    }
}
