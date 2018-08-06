package testcases.T030_buck_classusage_motif;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static Set<String> loadedClasses;

    public static void run() {
        assertThat(loadedClasses).doesNotContain("external.T030_buck_classusage_motif.A");
    }
}
