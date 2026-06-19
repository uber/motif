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
package testcases.T079_observer;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.List;
import motif.observe.Observer;
import motif.observe.MotifObserver;

public class Test {

    public static void run() {
        // Clear any existing observers
        MotifObserver.clearAll();

        // Create a test observer to track events
        TestObserver observer = new TestObserver();
        MotifObserver.register(observer);

        // Create the scope - should trigger onScopeInitializing
        Scope scope = new ScopeImpl();

        // Verify initialization event was fired
        assertThat(observer.events).hasSize(1);
        assertThat(observer.events.get(0)).startsWith("onScopeInitializing:");
        assertThat(observer.events.get(0)).contains("testcases.T079_observer.Scope");

        // Call factory methods - should trigger onProvideStart and onProvideComplete
        String str = scope.string();
        assertThat(str).isEqualTo("test");

        // Verify provide events were fired for string() method
        assertThat(observer.events).hasSize(3);
        assertThat(observer.events.get(1)).startsWith("onProvideStart:");
        assertThat(observer.events.get(1)).contains("string");
        assertThat(observer.events.get(2)).startsWith("onProvideComplete:");
        assertThat(observer.events.get(2)).contains("string");

        // Call another factory method
        Integer num = scope.number();
        assertThat(num).isEqualTo(42);

        // Verify provide events were fired for number() method
        // Note: the internal provider method is named "integer" (based on return type)
        assertThat(observer.events).hasSize(5);
        assertThat(observer.events.get(3)).startsWith("onProvideStart:");
        assertThat(observer.events.get(3)).contains("integer");
        assertThat(observer.events.get(4)).startsWith("onProvideComplete:");
        assertThat(observer.events.get(4)).contains("integer");

        // Test unregister
        MotifObserver.unregister(observer);
        Scope scope2 = new ScopeImpl();
        scope2.string();

        // Events list should not have grown since observer was unregistered
        assertThat(observer.events).hasSize(5);

        // Clean up
        MotifObserver.clearAll();
    }

    private static class TestObserver implements Observer {
        final List<String> events = new ArrayList<>();

        @Override
        public void onScopeInitializing(String scopeClassName) {
            events.add("onScopeInitializing: " + scopeClassName);
        }

        @Override
        public void onProvideStart(String scopeClassName, String methodName) {
            events.add("onProvideStart: " + scopeClassName + " - " + methodName);
        }

        @Override
        public void onProvideComplete(String scopeClassName, String methodName) {
            events.add("onProvideComplete: " + scopeClassName + " - " + methodName);
        }
    }
}
