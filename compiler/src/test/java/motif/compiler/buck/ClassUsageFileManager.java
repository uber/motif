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
