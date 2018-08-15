package motif.daggercomparison.motif;

import android.view.ViewGroup;
import motif.Scope;

@Scope
public interface RootScope {

    RootController controller();

    LoggedInScope loggedIn(ViewGroup parentViewGroup);

    @motif.Objects
    abstract class Objects {

        abstract RootController controller();

        RootView view(ViewGroup viewGroup) {
            return RootView.create(viewGroup);
        }
    }
}
