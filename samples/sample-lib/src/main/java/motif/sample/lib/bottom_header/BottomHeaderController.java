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
package motif.sample.lib.bottom_header;

import android.view.ViewGroup;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.multiselect.MultiSelector;
import motif.sample_lib.R;

public class BottomHeaderController extends Controller<BottomHeaderView> {

  private final MultiSelector multiSelector;

  public BottomHeaderController(ViewGroup parent, MultiSelector multiSelector) {
    super(parent, R.layout.bottomheader);
    this.multiSelector = multiSelector;
  }

  @Override
  protected void onAttach() {
    view.setListener(multiSelector::clearSelected);

    multiSelector
        .selected()
        .as(autoDispose())
        .subscribe(
            photos -> {
              view.setSelectedCount(photos.size());
            });
  }
}
