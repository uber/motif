package com.uber.motif.sample.app.root;

import android.content.Context;
import android.view.ViewGroup;

import com.uber.motif.Scope;
import com.uber.motif.sample.app.bottom_sheet.BottomSheetScope;
import com.uber.motif.sample.app.photo_grid.PhotoGridScope;
import com.uber.motif.sample.lib.controller.ControllerObjects;
import com.uber.motif.sample.lib.db.Database;
import com.uber.motif.sample.lib.multiselect.MultiSelector;

@Scope
public interface RootScope {

    RootView view();

    PhotoGridScope photoList(ViewGroup parent);

    BottomSheetScope bottomSheet(ViewGroup parent);

    abstract class Objects extends ControllerObjects<RootController, RootView> {

        public abstract Database database();
        public abstract MultiSelector multiSelector();
    }

    interface Parent {

        Context context();
    }
}
