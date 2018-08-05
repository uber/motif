package com.uber.motif.compiler;

import com.google.common.truth.Truth;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import dagger.internal.codegen.ComponentProcessor;
import motif.compiler.Processor;
import motif.ir.graph.errors.GraphErrors;
import motif.stubcompiler.StubProcessor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
    private final boolean hasExternalSources;

    @SuppressWarnings("unused")
    public TestHarness(File commonDir, File testCaseDir, File externalDir, String testName) {
        this.testCaseDir = testCaseDir;
        this.commonDir = commonDir;
        this.externalDir = externalDir;
        this.testClassName = "testcases." + testName + ".Test";
        this.hasExternalSources = externalDir.listFiles() != null && externalDir.listFiles().length > 0;
    }

    @Test
    public void test() throws Throwable {
        JavaFileObject[] files = Stream.of(Files.walk(testCaseDir.toPath()), Files.walk(commonDir.toPath()))
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

        if (hasExternalSources) compileExternalSources();

        Processor processor = new Processor();
        Compilation compilation = compiler(processor).compile(files);

        Class<?> testClass;
        if (compilation.status() == Compilation.Status.FAILURE) {
            Compilation noProcessorCompilation = compiler(new StubProcessor()).compile(files);
            testClass = compilationClassLoader(noProcessorCompilation).loadClass(testClassName);
        } else {
            testClass = compilationClassLoader(compilation).loadClass(testClassName);
        }

        try {
            Field expectedException = testClass.getField("errors");
            assertThat(compilation).failed();
            GraphErrors errors = processor.getErrors();
            Truth.assertThat(errors).isNotNull();
            expectedException.set(null, errors);
        } catch (NoSuchFieldException ignore) {
            assertThat(compilation).succeeded();
        }

        try {
            testClass.getMethod("run").invoke(null);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private ClassLoader compilationClassLoader(Compilation compilation) {
        if (hasExternalSources) {
            return new CompilationClassLoader(externalSourceCompiler.classLoader, compilation);
        } else {
            return new CompilationClassLoader(compilation);
        }
    }

    private Compiler compiler(javax.annotation.processing.Processor processor) {
        Compiler compiler = javac().withProcessors(processor, new ComponentProcessor());
        if (hasExternalSources) {
            compiler = compiler.withClasspathFrom(externalSourceCompiler.classLoader);
        }
        return compiler;
    }

    private void compileExternalSources() throws IOException {
        Compilation compilation = externalSourceCompiler.compile(externalDir);
        assertThat(compilation).succeeded();
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
