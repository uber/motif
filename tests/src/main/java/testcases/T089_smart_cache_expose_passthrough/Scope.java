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
package testcases.T089_smart_cache_expose_passthrough;

import motif.Creatable;

// Regression test: @Expose must win over the abstract-passthrough optimization.
//
// foo() is an abstract 1-parameter (Binds-shaped) method, which the passthrough rule skips
// because a cast costs nothing to repeat. But a passthrough forwards whatever instance its own
// provider hands it at that call - and fooImpl() is itself uncached here, so two independent
// calls to foo() would forward two different FooImpl instances. With @Expose on foo(), every
// child scope must see one instance, so the passthrough rule must not skip it.
@motif.Scope(cachingStrategy = motif.CachingStrategy.SMART_CACHE)
public interface Scope extends Creatable<Scope.Dependencies> {

    ChildScope child();

    ChildScope otherChild();

    @motif.Objects
    abstract class Objects {

        // Regression target: exposed passthrough. Must be cached despite the Binds shape.
        @motif.Expose
        abstract Foo foo(FooImpl impl);

        // Single consumer (foo), not exposed, so this stays uncached. That is what makes the
        // bug observable: an uncached upstream means an uncached passthrough forwards a fresh
        // instance on every call.
        FooImpl fooImpl() {
            return new FooImpl();
        }
    }

    interface Dependencies {}
}
