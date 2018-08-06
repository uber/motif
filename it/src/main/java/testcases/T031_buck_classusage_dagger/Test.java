package testcases.T031_buck_classusage_dagger;

import external.T031_buck_classusage_dagger.Child;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static Set<String> loadedClasses;

    public static void run() {
        assertThat(loadedClasses).doesNotContain("external.T031_buck_classusage_dagger.A");
    }

    @dagger.Component
    interface Component extends Child.Parent {}
}
