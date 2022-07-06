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
package motif.sample.app.photo_grid_item;

import butterknife.ButterKnife;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.db.Photo;
import motif.sample.lib.multiselect.MultiSelector;

public class PhotoGridItemController extends Controller<PhotoGridItemView> {

  private final PhotoGridItemScope scope;
  private final MultiSelector multiSelector;
  private final Photo photo;

  PhotoGridItemController(
      PhotoGridItemScope scope, PhotoGridItemView view, MultiSelector multiSelector, Photo photo) {
    super(view, false);
    this.scope = scope;
    this.multiSelector = multiSelector;
    this.photo = photo;
    ButterKnife.bind(this, view);
  }

  @Override
  protected void onAttach() {
    view.setPhoto(photo);

    view.clicks()
        .as(autoDispose())
        .subscribe(
            o -> {
              if (multiSelector.isSelected(photo)) {
                multiSelector.deselect(photo);
              } else {
                multiSelector.select(photo);
              }
            });

    multiSelector
        .selected()
        .as(autoDispose())
        .subscribe(
            photos -> {
              view.setSelected(multiSelector.isSelected(photo));
            });
  }
}
