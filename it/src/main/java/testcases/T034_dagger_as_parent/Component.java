package testcases.T034_dagger_as_parent;

import dagger.Provides;

import javax.inject.Named;

@dagger.Component(modules = Component.Module.class)
public interface Component extends ScopeImpl.Dependencies {

    @dagger.Module
    class Module {

        @Named("dagger")
        @Provides
        String dagger() {
            return "dagger";
        }
    }
}
