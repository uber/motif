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
package testcases.T036_dagger_spread;

import motif.Spread;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    String string();

    @Named("motif")
    String motif();

    @motif.Objects
    class Objects {

        @Named("motif")
        String motif() {
            return "motif";
        }

        String string(@Named("dagger") String dagger) {
            return "motif" + dagger;
        }

        @Spread
        Component component(Scope scope) {
            return DaggerComponent.builder()
                    .module(new Component.Module())
                    .scope(scope)
                    .build();
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
