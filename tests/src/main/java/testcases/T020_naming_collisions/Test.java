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
package testcases.T020_naming_collisions;

import motif.Expose;
import motif.Scope;

import javax.inject.Named;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static void run() {
        Parent parent = new TestGrandparentImpl().p();
        ChildA a = parent.a();
        ChildB b = parent.b();
        ChildC c = parent.c();
        ChildD d = parent.d();

        assertThat(a.string()).isEqualTo("a");
        assertThat(b.string()).isEqualTo("b");
        assertThat(c.c()).isNotNull();
        assertThat(d.d()).isNotNull();
    }

    @Scope
    interface Grandparent {

        Parent p();

        @motif.Objects
        abstract class Objects {

            @Expose
            abstract testcases.T020_naming_collisions.c.SomeDependency c();

            @Expose
            abstract testcases.T020_naming_collisions.d.SomeDependency d();

            @Expose
            @Named("A")
            String a() {
                return "a";
            }

            @Expose
            @Named("B")
            String b() {
                return "b";
            }
        }

        @motif.Dependencies
        interface Dependencies {}
    }

    @Scope
    interface Parent {

        ChildA a();
        ChildB b();
        ChildC c();
        ChildD d();
    }

    @Scope
    interface ChildA {
        @Named("A")
        String string();
    }

    @Scope
    interface ChildB {
        @Named("B")
        String string();
    }

    @Scope
    interface ChildC {

        testcases.T020_naming_collisions.c.SomeDependency c();
    }

    @Scope
    interface ChildD {

        testcases.T020_naming_collisions.d.SomeDependency d();
    }
}
