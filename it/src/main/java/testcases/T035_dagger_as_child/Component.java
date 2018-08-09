package testcases.T035_dagger_as_child;

import dagger.Provides;

import javax.inject.Named;

@dagger.Component(
        modules = Component.Module.class,
        dependencies = Component.Parent.class)
public interface Component {

    String string();

    @dagger.Module
    class Module {

        @Named("dagger")
        @Provides
        String dagger() {
            return "dagger";
        }

        @Provides
        String string(@Named("motif") String motif, @Named("dagger") String dagger) {
            return motif + dagger;
        }
    }

    interface Parent {

        @Named("motif")
        String motif();
    }
}
