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
import android.view.ViewGroup;

import motif.Expose;
import motif.Scope;
import motif.sample.app.bottom_sheet.BottomSheetScope;
import motif.sample.app.photo_grid.PhotoGridScope;
import motif.sample.lib.controller.ControllerObjects;
import motif.sample.lib.db.Database;
import motif.sample.lib.multiselect.MultiSelector;

@Scope
public interface RootScope {

    RootView view();

    PhotoGridScope photoList(ViewGroup parent);

    BottomSheetScope bottomSheet(ViewGroup parent);

    @motif.Objects
    abstract class Objects extends ControllerObjects<RootController, RootView> {

        @Expose
        abstract Database database();

        @Expose
        abstract MultiSelector multiSelector();
    }

    @motif.Dependencies
    interface Dependencies {

        Context context();
    }
}
