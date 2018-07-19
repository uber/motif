package com.uber.motif.sample.app.bottom_sheet;

import android.view.ViewGroup;

import com.uber.motif.Scope;
import com.uber.motif.sample.app.bottom_header.BottomHeaderScope;
import com.uber.motif.sample.app.photo_list.PhotoListScope;
import com.uber.motif.sample.lib.controller.ControllerObjects;

@Scope
public interface BottomSheetScope {

    BottomSheetView view();

    BottomHeaderScope header(ViewGroup parent);

    PhotoListScope photoList(ViewGroup parent);

    abstract class Objects extends ControllerObjects<BottomSheetController, BottomSheetView> {}
}
