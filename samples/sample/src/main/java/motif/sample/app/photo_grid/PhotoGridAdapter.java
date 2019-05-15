/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
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
package motif.sample.app.photo_grid;


import android.view.ViewGroup;

import motif.sample.app.photo_grid_item.PhotoGridItemView;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.controller.ControllerAdapter;
import motif.sample.lib.db.Photo;
import motif.sample.lib.photo.PhotoDiffItemCallback;

class PhotoGridAdapter extends ControllerAdapter<Photo, PhotoGridItemView> {

    PhotoGridAdapter(PhotoGridScope scope) {
        super(new ControllerAdapter.Factory<Photo, PhotoGridItemView>() {
            @Override
            public Controller controller(PhotoGridItemView view, Photo item) {
                return scope.photoRow(view, item).controller();
            }

            @Override
            public PhotoGridItemView view(ViewGroup parent) {
                return PhotoGridItemView.create(parent);
            }
        }, new PhotoDiffItemCallback());
    }
}
