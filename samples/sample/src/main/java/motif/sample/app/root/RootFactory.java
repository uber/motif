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
package motif.sample.app.root;

import androidx.appcompat.app.AppCompatActivity;
import motif.Creatable;
import motif.NoDependencies;
import motif.Scope;

/**
 * This is a convenience Scope defined simply to build the RootScope. This additional layer is
 * useful since RootScope requires a Context that must be provided from outside of the graph.
 * RootFactory itself does not require any dependencies of its own and can be instantiated in
 * RootActivity via ScopeFactory.create(RootFactory.class).
 */
@Scope
public interface RootFactory extends Creatable<NoDependencies> {

  RootScope create(AppCompatActivity activity);
}
