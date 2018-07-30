package testcases.E001_missing_dependency;

import motif.compiler.errors.MissingDependencies;
import common.DebugStringCorrespondence;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static MissingDependencies expectedException;

    public static void run() {
        assertThat(expectedException).isNotNull();
        assertThat(expectedException.getMissingDependencies())
                .comparingElementsUsing(new DebugStringCorrespondence())
                .containsExactly("java.lang.String");
    }
}
