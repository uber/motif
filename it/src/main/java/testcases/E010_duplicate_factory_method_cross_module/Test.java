package testcases.E010_duplicate_factory_method_cross_module;

import com.google.common.truth.Truth;
import common.DuplicateFactoryMethodsSubject;
import motif.ir.graph.errors.DuplicateFactoryMethodsError;
import motif.ir.graph.errors.GraphErrors;
import org.junit.Ignore;

// TODO This case doesn't work right now because we'd need to pass meta information about downstream factory methods.
// At higher levels, this is will be a significant volume of information we'd need to cache in compiled jars.
// Open question: Is it better to cache this information (would need to be for every scope) or to re-run full graph
// validation in each module?
@Ignore
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
