package com.uber.motif.compiler;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import dagger.internal.codegen.ComponentProcessor;
import motif.compiler.Processor;
import motif.compiler.errors.CompilationError;
import motif.stubcompiler.StubProcessor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.annotation.Nullable;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

@RunWith(Parameterized.class)
public class TestHarness {

    @Parameterized.Parameters(name = "{3}")
    public static Collection<Object[]> data() {
        File sourceRoot = new File("../it/src/main/java/");
        File testCaseRoot = new File(sourceRoot, "testcases");
        File externalRoot = new File(sourceRoot, "external");
        File commonDir = new File(sourceRoot, "common");
        File[] testCaseDirs = testCaseRoot.listFiles(TestHarness::isTestDir);
        if (testCaseDirs == null) throw new IllegalStateException("Could not find test case directories: " + testCaseRoot);
        return Arrays.stream(testCaseDirs)
                .map(file -> {
                    File externalDir = new File(externalRoot, file.getName());
                    return new Object[]{commonDir, file.getAbsoluteFile(), externalDir, file.getName()};
                })
                .collect(Collectors.toList());
    }

    private static boolean isTestDir(File file) {
        String filename = file.getName();
        return file.isDirectory()
                && file.listFiles().length > 0
                && (filename.startsWith("T") || filename.startsWith("E"));
    }

    @Rule public ExternalSourceCompiler externalSourceCompiler = new ExternalSourceCompiler();

    private final File testCaseDir;
    private final File commonDir;
    private final File externalDir;
    private final String testClassName;

    @Nullable private CompilationError error;

    @SuppressWarnings("unused")
    public TestHarness(File commonDir, File testCaseDir, File externalDir, String testName) {
        this.testCaseDir = testCaseDir;
        this.commonDir = commonDir;
        this.externalDir = externalDir;
        this.testClassName = "testcases." + testName + ".Test";
    }

    @Test
    public void test() throws Throwable {
        JavaFileObject[] files = Stream.of(Files.walk(testCaseDir.toPath()))
                .flatMap(Function.identity())
                .map(Path::toFile)
                .filter(file -> !file.isDirectory() && file.getName().endsWith(".java"))
                .map(file -> {
                    try {
                        return JavaFileObjects.forResource(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(JavaFileObject[]::new);

        Compiler compiler = javac().withProcessors(
                new Processor(error -> this.error = error),
                new ComponentProcessor());
        if (compileExternalSources()) {
            compiler = compiler.withOptions(ImmutableList.builder()
                    .addAll(compiler.options())
                    .add("-classpath")
                    .add(externalSourceCompiler.classpath)
                    .build());
        }
        Compilation compilation = compiler.compile(files);

        Class<?> testClass;
        if (compilation.status() == Compilation.Status.FAILURE) {
            Compilation noProcessorCompilation = javac().withProcessors(
                    new StubProcessor(),
                    new ComponentProcessor()).compile(files);
            testClass = new CompilationClassLoader(noProcessorCompilation).loadClass(testClassName);
            try {
                Field expectedException = testClass.getField("expectedError");
                expectedException.set(null, error);
            } catch (NoSuchFieldException ignore) {
                assertThat(compilation).succeeded();
            }
        } else {
            ClassLoader classLoader = new CompilationClassLoader(compilation);
            testClass = classLoader.loadClass(testClassName);
        }

        try {
            testClass.getMethod("run").invoke(null);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private boolean compileExternalSources() throws IOException {
        File[] files = externalDir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }

        Compilation compilation = externalSourceCompiler.compile(
                externalDir,
                new Processor(),
                new ComponentProcessor());
        assertThat(compilation).succeeded();
        return true;
    }

    static List<JavaFileObject> javaFileObjects(File dir) throws IOException {
        return Stream.of(Files.walk(dir.toPath()))
                .flatMap(Function.identity())
                .map(Path::toFile)
                .filter(file -> !file.isDirectory() && file.getName().endsWith(".java"))
                .map(file -> {
                    try {
                        return JavaFileObjects.forResource(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
