package com.uber.motif.compiler;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationFactory;
import com.google.testing.compile.Compiler;
import org.junit.rules.TemporaryFolder;

import javax.annotation.processing.Processor;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExternalSourceCompiler extends TemporaryFolder {

    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public String classpath;

    private File compilerOutputDir;

    @Override
    protected void before() throws Throwable {
        super.before();
        compilerOutputDir = newFolder();
        String currentClasspath = System.getProperty("java.class.path");
        classpath = currentClasspath + ":" + compilerOutputDir.getAbsolutePath();
    }

    Compilation compile(
            File dir,
            Processor... processors) throws IOException {
        List<JavaFileObject> files = TestHarness.javaFileObjects(dir);
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        CollectingFileManager fileManager = new CollectingFileManager(diagnosticCollector);
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnosticCollector,
                ImmutableList.of("-d", compilerOutputDir.getAbsolutePath()),
                Collections.emptyList(),
                files);
        task.setProcessors(ImmutableList.copyOf(processors));
        boolean succeeded = task.call();
        return CompilationFactory.create(
                Compiler.javac(),
                files,
                succeeded,
                diagnosticCollector.getDiagnostics(),
                fileManager.getOutputFiles());
    }

    private class CollectingFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        private final Set<JavaFileObject> outputFiles = new HashSet<>();

        public CollectingFileManager(DiagnosticCollector<JavaFileObject> diagnosticCollector) {
            super(compiler.getStandardFileManager(diagnosticCollector, null, null));
        }

        public Set<JavaFileObject> getOutputFiles() {
            return outputFiles;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            JavaFileObject javaFile = super.getJavaFileForOutput(location, className, kind, sibling);
            outputFiles.add(javaFile);
            return javaFile;
        }
    }
}
