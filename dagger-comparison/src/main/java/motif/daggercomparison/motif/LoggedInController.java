package motif.daggercomparison.motif;

public class LoggedInController {

    private final LoggedInScope scope;
    private final LoggedInView view;

    public LoggedInController(LoggedInScope scope, LoggedInView view) {
        this.scope = scope;
        this.view = view;
    }

    public void onStart() {
        // ...
    }
}
