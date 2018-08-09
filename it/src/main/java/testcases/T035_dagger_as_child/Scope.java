package testcases.T035_dagger_as_child;

import javax.inject.Named;

@motif.Scope
public interface Scope extends Component.Parent {

    @motif.Objects
    class Objects {

        @Named("motif")
        String string() {
            return "motif";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
