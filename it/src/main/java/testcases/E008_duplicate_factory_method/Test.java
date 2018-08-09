package testcases.E008_duplicate_factory_method;

import com.google.common.truth.Truth;
import common.DuplicateFactoryMethodsSubject;
import motif.ir.graph.errors.DuplicateFactoryMethodsError;
import motif.ir.graph.errors.GraphValidationErrors;

public class Test {

    public static GraphValidationErrors errors;

    public static void run() {
        DuplicateFactoryMethodsError error = errors.getDuplicateFactoryMethodsError();
        Truth.assertThat(error).isNotNull();
        DuplicateFactoryMethodsSubject.assertThat(error)
                .with("sa", "sb")
                .with("sb", "sa")
                .matches();
    }
}
