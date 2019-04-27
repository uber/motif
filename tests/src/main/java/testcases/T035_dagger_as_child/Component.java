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
package testcases.T035_dagger_as_child;

import dagger.Provides;

import javax.inject.Named;

@dagger.Component(
        modules = Component.Module.class,
        dependencies = Component.Parent.class)
public interface Component {

    String string();

    @dagger.Module
    class Module {

        @Named("dagger")
        @Provides
        String dagger() {
            return "dagger";
        }

        @Provides
        String string(@Named("motif") String motif, @Named("dagger") String dagger) {
            return motif + dagger;
        }
    }

    interface Parent {

        @Named("motif")
        String motif();
    }
}
