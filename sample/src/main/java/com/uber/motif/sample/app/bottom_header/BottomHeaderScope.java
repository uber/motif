package com.uber.motif.sample.app.bottom_header;

import com.uber.motif.Scope;

@Scope
public interface BottomHeaderScope {

    BottomHeaderView view();

    abstract class Objects {

        abstract BottomHeaderController controller();

        BottomHeaderView view(BottomHeaderController controller) {
            return controller.getView();
        }
    }
}
