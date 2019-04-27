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
package com.google.testing.compile;

import com.google.common.collect.ImmutableList;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MotifTestCompiler {

    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public Result compile(
            @Nullable File classpathDir,
            @Nullable File outDir,
            @Nullable Processor motifProcessor,
            File dir) throws IOException {
        List<JavaFileObject> files = javaFileObjects(dir);
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

        ClassUsageFileManager classUsageFileManager = new ClassUsageFileManager(diagnosticCollector, classpathDir);
        StandardJavaFileManager fileManager = classUsageFileManager;
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

    // TODO Remove Test.java filter after deletion of old test code.
    private static List<JavaFileObject> javaFileObjects(File dir) throws IOException {
        boolean skipTest = new File(dir, "ERROR.txt").exists();
        return Files.walk(dir.toPath())
                .map(Path::toFile)
                .filter(file -> !file.isDirectory()
                        && file.getName().endsWith(".java")
                        && !file.getName().endsWith("ScopeImpl.java")
                        && (!skipTest || !file.getName().equals("Test.java")))
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
