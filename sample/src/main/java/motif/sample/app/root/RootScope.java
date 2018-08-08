package motif.sample.app.root;

import android.content.Context;
import android.view.ViewGroup;

import motif.Expose;
import motif.Scope;
import motif.sample.app.bottom_sheet.BottomSheetScope;
import motif.sample.app.photo_grid.PhotoGridScope;
import motif.sample.lib.controller.ControllerObjects;
import motif.sample.lib.db.Database;
import motif.sample.lib.multiselect.MultiSelector;

@Scope
public interface RootScope {

    RootView view();

    PhotoGridScope photoList(ViewGroup parent);

    BottomSheetScope bottomSheet(ViewGroup parent);

    @motif.Objects
    abstract class Objects extends ControllerObjects<RootController, RootView> {

        @Expose
        abstract Database database();

        @Expose
        abstract MultiSelector multiSelector();
    }

    @motif.Dependencies
    interface Dependencies {

        Context context();
    }
}
