package motif.daggercomparison.dagger;

import javax.inject.Inject;

@RootComponent.Scope
public class RootController {

    private final RootComponent component;
    private final RootView view;

    @Inject
    public RootController(RootComponent component, RootView view) {
        this.component = component;
        this.view = view;
    }

    public void onStart() {
        LoggedInController controller = component.loggedIn()
                .viewGroup(view)
                .build()
                .controller();
        controller.onStart();
    }
}
