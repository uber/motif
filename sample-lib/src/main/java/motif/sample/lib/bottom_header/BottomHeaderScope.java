package motif.sample.lib.bottom_header;

import motif.Scope;
import motif.sample.lib.controller.ControllerObjects;

@Scope
public interface BottomHeaderScope {

    BottomHeaderView view();

    @motif.Objects
    abstract class Objects extends ControllerObjects<BottomHeaderController, BottomHeaderView> {}
}
