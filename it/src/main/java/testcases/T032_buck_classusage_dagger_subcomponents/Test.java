package testcases.T032_buck_classusage_dagger_subcomponents;

import external.T032_buck_classusage_dagger_subcomponents.Child;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static Set<String> loadedClasses;

    public static void run() {
        assertThat(loadedClasses).contains("external.T032_buck_classusage_dagger_subcomponents.A");
    }

    @dagger.Component
    interface Component {

        Child child();
    }
}
