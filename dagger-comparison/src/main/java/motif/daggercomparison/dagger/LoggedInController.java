package motif.daggercomparison.dagger;

import javax.inject.Inject;

@LoggedInComponent.Scope
public class LoggedInController {

    private final LoggedInComponent component;
    private final LoggedInView view;

    @Inject
    public LoggedInController(LoggedInComponent component, LoggedInView view) {
        this.component = component;
        this.view = view;
    }

    public void onStart() {
        // ...
    }
}
