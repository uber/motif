package motif.sample.app.bottom_sheet;

import android.view.ViewGroup;

import motif.Scope;
import motif.sample.app.bottom_header.BottomHeaderScope;
import motif.sample.app.photo_list.PhotoListScope;
import motif.sample.lib.controller.ControllerObjects;

@Scope
public interface BottomSheetScope {

    BottomSheetView view();

    BottomHeaderScope header(ViewGroup parent);

    PhotoListScope photoList(ViewGroup parent);

    abstract class Objects extends ControllerObjects<BottomSheetController, BottomSheetView> {}
}
