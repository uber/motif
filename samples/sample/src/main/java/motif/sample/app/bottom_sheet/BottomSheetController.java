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
