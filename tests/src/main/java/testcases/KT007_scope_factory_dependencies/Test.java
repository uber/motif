/*
 * Copyright (c) 2023 Uber Technologies, Inc.
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
package testcases.KT007_scope_factory_dependencies;

import motif.ScopeFactory;
import org.jetbrains.annotations.NotNull;

public class Test {

    public static void run() {
        ScopeFactory.create(
                Scope.class, new Scope.Dependencies() {
                    @NotNull
                    @Override
                    public String string() {
                        return "s";
                    }
                }
        );
    }
}
