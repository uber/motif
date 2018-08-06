package external.T032_buck_classusage_dagger_subcomponents;

import dagger.Provides;
import dagger.Subcomponent;

@Subcomponent(modules = Child.Module.class)
public interface Child {

    String string();

    @dagger.Module
    abstract class Module {

        @Provides
        static A a() {
            return new A();
        }

        @Provides
        static String string(A a) {
            return "a";
        }
    }
}
