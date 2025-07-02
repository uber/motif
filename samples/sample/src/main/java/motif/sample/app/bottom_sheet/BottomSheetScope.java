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
package motif.sample.app.bottom_sheet;

import android.view.ViewGroup;
import motif.Scope;
import motif.sample.app.photo_grid.PhotoGridScope;
import motif.sample.app.photo_list.PhotoListScope;
import motif.sample.lib.bottom_header.BottomHeaderScope;
import motif.sample.lib.controller.ControllerObjects;

@Scope(useNullFieldInitialization = true)
public interface BottomSheetScope {

  BottomSheetView view();

  BottomHeaderScope header(ViewGroup parent);

  PhotoListScope photoList(ViewGroup parent);

  PhotoGridScope photoGrid(ViewGroup parent);

  @motif.Objects
  abstract class Objects extends ControllerObjects<BottomSheetController, BottomSheetView> {}
}
