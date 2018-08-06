package external.T031_buck_classusage_dagger;

import dagger.Component;
import dagger.Provides;

@Component(
        dependencies = Child.Parent.class,
modules = Child.Module.class)
public interface Child {

    String string();

    @dagger.Module
    class Module {

        @Provides
        A a() {
            return new A();
        }

        @Provides
        String string(A a) {
            return "a";
        }
    }

    interface Parent {}
}
