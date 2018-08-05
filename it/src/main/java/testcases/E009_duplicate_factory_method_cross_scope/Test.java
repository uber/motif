package testcases.E009_duplicate_factory_method_cross_scope;

import com.google.common.truth.Truth;
import common.DuplicateFactoryMethodsSubject;
import motif.ir.graph.errors.DuplicateFactorMethodsError;
import motif.ir.graph.errors.GraphErrors;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        DuplicateFactorMethodsError error = errors.getDuplicateFactorMethodsError();
        Truth.assertThat(error).isNotNull();
        DuplicateFactoryMethodsSubject.assertThat(error)
                .with("sb", "sa")
                .matches();
    }
}
