package com.uber.motif.sample.app.bottom_sheet;

import android.view.ViewGroup;

import com.uber.motif.Scope;
import com.uber.motif.sample.app.bottom_header.BottomHeaderScope;
import com.uber.motif.sample.app.photo_list.PhotoListScope;

@Scope
public interface BottomSheetScope {

    BottomSheetView view();

    BottomHeaderScope header(ViewGroup parent);

    PhotoListScope photoList(ViewGroup parent);

    abstract class Objects {

        abstract BottomSheetController controller();

        BottomSheetView view(BottomSheetController controller) {
            return controller.getView();
        }
    }
}
