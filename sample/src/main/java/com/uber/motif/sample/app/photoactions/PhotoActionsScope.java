package com.uber.motif.sample.app.photoactions;

import com.uber.motif.Scope;

@Scope
public interface PhotoActionsScope {

    PhotoActionsView view();

    abstract class Objects {

        abstract PhotoActionsController controller();

        PhotoActionsView view(PhotoActionsController controller) {
            return controller.getView();
        }
    }
}
