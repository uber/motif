package motif.sample.app.root;

import android.content.Context;

/**
 * This is a convenience Scope defined simply to build the RootScope. This additional layer is useful since RootScope
 * requires a Context that must be provided from outside of the graph. RootFactory can declare an empty @Dependencies
 * interface and pass the ViewGroup in to the RootScope child method. In RootActivity, we now have a nice API to
 * instantiate the RootScope.
 */
@motif.Scope
public interface RootFactory {

    RootScope create(Context context);

    @motif.Dependencies
    interface Dependencies {}
}
