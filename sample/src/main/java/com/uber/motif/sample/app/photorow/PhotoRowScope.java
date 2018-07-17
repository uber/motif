package com.uber.motif.sample.app.photorow;

import android.view.ViewGroup;
import com.uber.motif.Scope;
import com.uber.motif.sample.app.photoactions.PhotoActionsScope;

@Scope
public interface PhotoRowScope {

    PhotoRowController controller();

    PhotoActionsScope actions(ViewGroup parent);

    abstract class Objects {

        abstract PhotoRowController controller();
    }
}
