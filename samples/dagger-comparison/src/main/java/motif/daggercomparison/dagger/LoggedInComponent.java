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
package motif.daggercomparison.dagger;

import android.view.ViewGroup;
import dagger.BindsInstance;
import dagger.Provides;
import dagger.Subcomponent;

import javax.inject.Qualifier;

@LoggedInComponent.Scope
@Subcomponent(modules = LoggedInComponent.Module.class)
public interface LoggedInComponent {

    LoggedInController controller();

    @Scope
    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        Builder viewGroup(@LoggedIn ViewGroup parent);

        LoggedInComponent build();
    }

    @dagger.Module
    abstract class Module {

        @Scope
        @Provides
        static LoggedInView view(@LoggedIn ViewGroup parent) {
            return LoggedInView.create(parent);
        }
    }

    @javax.inject.Scope
    @interface Scope {}

    @Qualifier
    @interface LoggedIn {}
}
