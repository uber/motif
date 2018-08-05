package testcases.E007_unprocessed_scope;

import com.google.common.truth.Truth;
import external.E007_unprocessed_scope.Child;
import motif.ir.graph.errors.GraphErrors;
import motif.ir.graph.errors.UnprocessedScopeError;
import motif.ir.source.base.Type;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        UnprocessedScopeError error = errors.getUnprocessedScopeError();
        Truth.assertThat(error).isNotNull();
        Truth.assertThat(error.getScopeType()).isEqualTo(new Type(null, Child.class.getName()));
    }
}
