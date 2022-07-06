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

import motif.Scope;
import motif.sample.app.photo_grid_item.PhotoGridItemScope;
import motif.sample.app.photo_grid_item.PhotoGridItemView;
import motif.sample.lib.controller.ControllerObjects;
import motif.sample.lib.db.Photo;

@Scope
public interface PhotoGridScope {

  PhotoGridView view();

  PhotoGridItemScope photoRow(PhotoGridItemView view, Photo photo);

  @motif.Objects
  abstract class Objects extends ControllerObjects<PhotoGridController, PhotoGridView> {

    abstract PhotoGridAdapter adapter();
  }
}
