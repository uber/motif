package motif.sample.app.bottom_sheet;

import android.view.ViewGroup;

import motif.sample.R;
import motif.sample.lib.bottom_header.BottomHeaderView;
import motif.sample.app.photo_list.PhotoListView;
import motif.sample.lib.controller.Controller;

public class BottomSheetController extends Controller<BottomSheetView> {

    private final BottomSheetScope scope;

    public BottomSheetController(
            BottomSheetScope scope,
            ViewGroup parent) {
        super(parent, R.layout.bottomsheet);
        this.scope = scope;
    }

    @Override
    protected void onAttach() {
        BottomHeaderView headerView = scope.header(view).view();
        view.showHeader(headerView);

        PhotoListView photoListView = scope.photoList(this.view).view();
        view.showPhotoList(photoListView);
    }
}
