package testcases.T030_buck_classusage_motif;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static Set<String> loadedClasses;

    public static void run() {
        // This is not ideal, but is a trade-off that we're documenting via this integration test.
        assertThat(loadedClasses).contains("external.T030_buck_classusage_motif.A");
    }
}
