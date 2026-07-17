/*
 * Copyright (c) 2025 Uber Technologies, Inc.
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
package testcases.T081_smart_cache_do_not_cache_wrapper;

import testcases.T081_smart_cache_do_not_cache_wrapper.dependency.DoNotCacheDep;
import testcases.T081_smart_cache_do_not_cache_wrapper.dependency.ExposedDep;
import testcases.T081_smart_cache_do_not_cache_wrapper.dependency.NotExposedDep;

public class DoNotCacheWrapper {
    public final ExposedDep exposedDep;
    public final NotExposedDep notExposedDep;
    public final DoNotCacheDep doNotCacheDep;

    public DoNotCacheWrapper(
            ExposedDep exposedDep,
            NotExposedDep notExposedDep,
            DoNotCacheDep doNotCacheDep) {
        this.exposedDep = exposedDep;
        this.notExposedDep = notExposedDep;
        this.doNotCacheDep = doNotCacheDep;
    }
}
