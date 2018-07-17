package com.uber.motif.sample.fileactions;

import com.uber.motif.Scope;

@Scope
public interface FileActionsScope {

    FileActionsView view();

    abstract class Objects {

        abstract FileActionsController controller();

        FileActionsView view(FileActionsController controller) {
            return controller.getView();
        }
    }
}
