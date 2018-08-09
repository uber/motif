package motif.sample.app.bottom_sheet;

import android.view.ViewGroup;

import motif.Scope;
import motif.sample.lib.bottom_header.BottomHeaderScope;
import motif.sample.app.photo_grid.PhotoGridScope;
import motif.sample.app.photo_list.PhotoListScope;
import motif.sample.lib.controller.ControllerObjects;

@Scope
public interface BottomSheetScope {

    BottomSheetView view();

    BottomHeaderScope header(ViewGroup parent);

    PhotoListScope photoList(ViewGroup parent);

    PhotoGridScope photoGrid(ViewGroup parent);

    @motif.Objects
    abstract class Objects extends ControllerObjects<BottomSheetController, BottomSheetView> {}
}
