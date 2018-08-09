package testcases.T036_dagger_spread;

import dagger.Provides;

import javax.inject.Named;

@dagger.Component(
        modules = Component.Module.class,
        dependencies = Scope.class)
public interface Component {

    @Named("dagger")
    String dagger();

    @dagger.Module
    class Module {

        @Named("dagger")
        @Provides
        String dagger(@Named("motif") String motif) {
            return "dagger" + motif;
        }
    }
}
