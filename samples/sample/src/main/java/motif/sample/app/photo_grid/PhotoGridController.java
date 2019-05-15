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

import motif.sample.R;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.db.Database;

public class PhotoGridController extends Controller<PhotoGridView> {

    private final Database database;
    private final PhotoGridAdapter adapter;

    public PhotoGridController(
            ViewGroup parent,
            Database database,
            PhotoGridAdapter adapter) {
        super(parent, R.layout.photo_grid);
        this.database = database;
        this.adapter = adapter;
    }

    @Override
    protected void onAttach() {
        database.allPhotos().as(autoDispose())
                .subscribe(photos -> {
                    view.setAdapter(adapter);
                    adapter.submitList(photos);
                });
    }
}
