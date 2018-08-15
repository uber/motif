package motif.daggercomparison.motif;

import android.view.ViewGroup;
import motif.Scope;

@Scope
public interface LoggedInScope {

    LoggedInController controller();

    @motif.Objects
    abstract class Objects {

        abstract LoggedInController controller();

        LoggedInView view(ViewGroup parentViewGroup) {
            return LoggedInView.create(parentViewGroup);
        }
    }
}
