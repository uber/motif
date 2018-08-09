package com.google.testing.compile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import dagger.internal.codegen.ComponentProcessor;
import motif.compiler.buck.ClassUsageFileManager;

import javax.annotation.Nullable;
import javax.annotation.processing.Processor;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MotifTestCompiler {

    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public Result compile(
            @Nullable File classpathDir,
            @Nullable File outDir,
            @Nullable Processor motifProcessor,
            File... dirs) {
        List<JavaFileObject> files = javaFileObjects(dirs);
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

        ClassUsageFileManager classUsageFileManager = new ClassUsageFileManager(diagnosticCollector, classpathDir);
        JavaFileManager fileManager = classUsageFileManager;
        if (outDir == null) {
            fileManager = new InMemoryJavaFileManager(fileManager);
        }
        CollectingFileManager collectingFileManager = new CollectingFileManager(fileManager);

        ImmutableList.Builder<String> optionsBuilder = ImmutableList.builder();
        if (classpathDir != null) {
            String currentClasspath = System.getProperty("java.class.path");
            String newClasspath = classpathDir + ":" + currentClasspath;
            optionsBuilder.add("-classpath", newClasspath);
        }
        if (outDir != null) {
            optionsBuilder.add("-d", outDir.getAbsolutePath());
        }

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                collectingFileManager,
                diagnosticCollector,
                optionsBuilder.build(),
                Collections.emptyList(),
                files);

        ImmutableList.Builder<Processor> processorsBuilder = ImmutableList.builder();
        if (motifProcessor != null) {
            processorsBuilder.add(motifProcessor);
        }
        processorsBuilder.add(new ComponentProcessor());

        task.setProcessors(processorsBuilder.build());
        boolean succeeded = task.call();
        Compilation compilation = new Compilation(
                Compiler.javac(),
                files,
                succeeded,
                diagnosticCollector.getDiagnostics(),
                collectingFileManager.getOutputFiles());
        return new Result(compilation, classUsageFileManager.getClassnames());
    }

    private class CollectingFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        private final Set<JavaFileObject> outputFiles = new HashSet<>();

        public CollectingFileManager(JavaFileManager fileManager) {
            super(fileManager);
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

    private static List<JavaFileObject> javaFileObjects(File... dirs) {
        return Stream.of(dirs)
                .filter(File::exists)
                .flatMap(file -> {
                    try {
                        return Files.walk(file.toPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(Path::toFile)
                .filter(file -> !file.isDirectory() && file.getName().endsWith(".java") && !file.getName().endsWith("ScopeImpl.java"))
                .map(file -> {
                    try {
                        return JavaFileObjects.forResource(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public static class Result {

        public final Compilation compilation;
        public final Set<String> classnames;

        public Result(Compilation compilation, Set<String> classnames) {
            this.compilation = compilation;
            this.classnames = classnames;
        }
    }
}
