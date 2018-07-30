package motif.sample.app.bottom_header;

import motif.Scope;
import motif.sample.lib.controller.ControllerObjects;

@Scope
public interface BottomHeaderScope {

    BottomHeaderView view();

    abstract class Objects extends ControllerObjects<BottomHeaderController, BottomHeaderView> {}
}
