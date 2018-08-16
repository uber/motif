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

/**
 * This is a convenience Scope defined simply to build the RootScope. This additional layer is useful since RootScope
 * requires a Context that must be provided from outside of the graph. RootFactory can declare an empty @Dependencies
 * interface and pass the ViewGroup in to the RootScope child method. In RootActivity, we now have a nice API to
 * instantiate the RootScope.
 */
@motif.Scope
public interface RootFactory {

    RootScope create(Context context);

    @motif.Dependencies
    interface Dependencies {}
}
