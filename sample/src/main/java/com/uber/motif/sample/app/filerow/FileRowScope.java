package com.uber.motif.sample.app.filerow;

import android.view.ViewGroup;
import com.uber.motif.Scope;
import com.uber.motif.sample.app.fileactions.FileActionsScope;

@Scope
public interface FileRowScope {

    FileRowController controller();

    FileActionsScope actions(ViewGroup parent);

    abstract class Objects {

        abstract FileRowController controller();
    }
}
