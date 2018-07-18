package com.uber.motif.sample.app.root;

import android.content.Context;

import com.uber.motif.sample.R;
import com.uber.motif.sample.app.photo_grid.PhotoGridView;
import com.uber.motif.sample.app.bottom_sheet.BottomSheetView;
import com.uber.motif.sample.lib.controller.Controller;
import com.uber.motif.sample.lib.db.Database;
import com.uber.motif.sample.lib.multiselect.MultiSelector;

class RootController extends Controller<RootView> {

    private final RootScope scope;
    private final Database database;
    private final MultiSelector multiSelector;

    RootController(
            RootScope scope,
            Context context,
            Database database,
            MultiSelector multiSelector) {
        super(context, R.layout.root);
        this.scope = scope;
        this.database = database;
        this.multiSelector = multiSelector;
    }

    @Override
    protected void onAttach() {
        database.populateIfNecessary()
                .as(autoDispose())
                .subscribe(() -> {
                    PhotoGridView photoGridView = scope.photoList(view).view();
                    view.showPhotos(photoGridView);
                });

        multiSelector.selected()
                .as(autoDispose())
                .subscribe(photos -> {
                    if (photos.isEmpty()) {
                        view.clearBottomSheet();
                    } else if (!view.isSectionViewShowing()) {
                        BottomSheetView bottomSheetView = scope.bottomSheet(this.view).view();
                        view.showBottomSheet(bottomSheetView);
                    }
                });
    }
}
