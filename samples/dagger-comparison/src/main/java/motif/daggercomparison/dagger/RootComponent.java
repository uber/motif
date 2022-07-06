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
package motif.daggercomparison.dagger;

import android.view.ViewGroup;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Provides;
import javax.inject.Qualifier;

@RootComponent.Scope
@Component(modules = RootComponent.Module.class)
public interface RootComponent {

  RootController controller();

  LoggedInComponent.Builder loggedIn();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder viewGroup(@Root ViewGroup parent);

    RootComponent build();
  }

  @dagger.Module
  abstract class Module {

    @Scope
    @Provides
    static RootView view(@Root ViewGroup parent) {
      return RootView.create(parent);
    }
  }

  @javax.inject.Scope
  @interface Scope {}

  @Qualifier
  @interface Root {}
}
