package motif.daggercomparison.motif;

public class RootController {

    private final RootScope scope;
    private final RootView view;

    public RootController(RootScope scope, RootView view) {
        this.scope = scope;
        this.view = view;
    }

    public void onStart() {
        LoggedInController controller = scope.loggedIn(view).controller();
        controller.onStart();
    }
}
