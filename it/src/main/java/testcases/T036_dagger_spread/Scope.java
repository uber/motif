package testcases.T036_dagger_spread;

import motif.Spread;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    String string();

    @Named("motif")
    String motif();

    @motif.Objects
    class Objects {

        @Named("motif")
        String motif() {
            return "motif";
        }

        String string(@Named("dagger") String dagger) {
            return "motif" + dagger;
        }

        @Spread
        Component component(Scope scope) {
            return DaggerComponent.builder()
                    .module(new Component.Module())
                    .scope(scope)
                    .build();
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
