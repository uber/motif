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
package motif.sample.app.photo_list;

import android.view.ViewGroup;
import motif.sample.R;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.multiselect.MultiSelector;

public class PhotoListController extends Controller<PhotoListView> {

  private final PhotoListAdapter adapter;
  private final MultiSelector multiSelector;

  public PhotoListController(
      ViewGroup parent, PhotoListAdapter adapter, MultiSelector multiSelector) {
    super(parent, R.layout.photo_list);
    this.adapter = adapter;
    this.multiSelector = multiSelector;
  }

  @Override
  protected void onAttach() {
    view.setAdapter(adapter);

    multiSelector.selected().as(autoDispose()).subscribe(adapter::submitList);
  }
}
