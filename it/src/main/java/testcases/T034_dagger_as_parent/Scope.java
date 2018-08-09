package testcases.T034_dagger_as_parent;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    String string();

    @motif.Objects
    class Objects {

        @Named("motif")
        String string() {
            return "motif";
        }

        String string(@Named("motif") String motif, @Named("dagger") String dagger) {
            return motif + dagger;
        }
    }
}
