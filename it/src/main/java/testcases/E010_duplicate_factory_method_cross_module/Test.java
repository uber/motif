package testcases.E010_duplicate_factory_method_cross_module;

import com.google.common.truth.Truth;
import common.DuplicateFactoryMethodsSubject;
import motif.ir.graph.errors.DuplicateFactoryMethodsError;
import motif.ir.graph.errors.GraphErrors;

public class Test {

    public static GraphErrors errors;

    public static void run() {
        DuplicateFactoryMethodsError error = errors.getDuplicateFactoryMethodsError();
        Truth.assertThat(error).isNotNull();
        DuplicateFactoryMethodsSubject.assertThat(error)
                .with("sb", "sa")
                .matches();
    }
}
