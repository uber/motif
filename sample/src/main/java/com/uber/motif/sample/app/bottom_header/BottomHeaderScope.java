package com.uber.motif.sample.app.bottom_header;

import com.uber.motif.Scope;
import com.uber.motif.sample.lib.controller.ControllerObjects;

@Scope
public interface BottomHeaderScope {

    BottomHeaderView view();

    abstract class Objects extends ControllerObjects<BottomHeaderController, BottomHeaderView> {}
}
