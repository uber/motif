/*
 * Copyright (c) 2018 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif.sample.app.root;

import android.content.Context;

import motif.sample.R;
import motif.sample.app.photo_grid.PhotoGridView;
import motif.sample.app.bottom_sheet.BottomSheetView;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.db.Database;
import motif.sample.lib.multiselect.MultiSelector;

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
