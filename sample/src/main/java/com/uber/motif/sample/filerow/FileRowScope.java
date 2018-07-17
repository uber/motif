package com.uber.motif.sample.filerow;

import android.view.ViewGroup;
import com.uber.motif.Scope;
import com.uber.motif.sample.fileactions.FileActionsScope;

@Scope
public interface FileRowScope {

    FileRowController controller();

    FileActionsScope actions(ViewGroup parent);

    abstract class Objects {

        abstract FileRowController controller();
    }
}
