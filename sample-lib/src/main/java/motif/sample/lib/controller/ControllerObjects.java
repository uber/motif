package motif.sample.lib.controller;

import android.view.View;

public abstract class ControllerObjects<C extends Controller<V>, V extends View> {

    public abstract C controller();

    public V view(C controller) {
        return controller.getView();
    }
}
