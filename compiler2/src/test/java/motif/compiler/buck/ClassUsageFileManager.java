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
package motif.compiler.buck;

import javax.annotation.Nullable;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.Locale;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ClassUsageFileManager extends ListenableFileManager {

    private final ClassUsageTracker tracker;

    public ClassUsageFileManager(
            DiagnosticCollector<JavaFileObject> diagnosticCollector,
            @Nullable File classpathDir) {
        super(ToolProvider.getSystemJavaCompiler().getStandardFileManager(diagnosticCollector, Locale.getDefault(), UTF_8));
        tracker = new ClassUsageTracker(classpathDir);
        addListener(tracker);
    }

    public Set<String> getClassnames() {
        return tracker.getClassnames();
    }
}
