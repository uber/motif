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
package motif.sample.lib.multiselect;


import com.jakewharton.rxrelay2.BehaviorRelay;
import motif.sample.lib.db.Photo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

public class MultiSelector {

    private final BehaviorRelay<List<Photo>> selected = BehaviorRelay.createDefault(Collections.emptyList());

    public void clearSelected() {
        selected.accept(Collections.emptyList());
    }

    public void select(Photo photo) {
        List<Photo> value = update();
        value.add(photo);
        selected.accept(value);
    }

    public void deselect(Photo photo) {
        List<Photo> value = update();
        value.remove(photo);
        selected.accept(value);
    }

    public boolean isSelected(Photo photo) {
        return selected.getValue().contains(photo);
    }

    public Observable<List<Photo>> selected() {
        return selected;
    }

    private ArrayList<Photo> update() {
        return new ArrayList<>(selected.getValue());
    }
}
