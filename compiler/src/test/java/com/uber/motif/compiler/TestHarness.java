package com.uber.motif.compiler;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import dagger.internal.codegen.ComponentProcessor;
import motif.compiler.Processor;
import motif.compiler.errors.CompilationError;
import motif.stubcompiler.StubProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.annotation.Nullable;
import javax.tools.JavaFileObject;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

@RunWith(Parameterized.class)
public class TestHarness {

    @Parameterized.Parameters(name = "{2}")
    public static Collection<Object[]> data() {
        File sourceRoot = new File("../it/src/main/java/");
        File testCaseRoot = new File(sourceRoot, "testcases");
        File commonDir = new File(sourceRoot, "common");
        File[] testCaseDirs = testCaseRoot.listFiles(TestHarness::isTestDir);
        if (testCaseDirs == null) throw new IllegalStateException("Could not find test case directories: " + testCaseRoot);
        return Arrays.stream(testCaseDirs)
                .map(file -> new Object[]{commonDir, file.getAbsoluteFile(), file.getName()})
                .collect(Collectors.toList());
    }

    private static boolean isTestDir(File file) {
        String filename = file.getName();
        return file.isDirectory()
                && file.listFiles().length > 0
                && (filename.startsWith("T") || filename.startsWith("E"));
    }

    private final File testCaseDir;
    private final File commonDir;
    private final String testClassName;

    @Nullable private CompilationError error;

    @SuppressWarnings("unused")
    public TestHarness(File commonDir, File testCaseDir, String testName) {
        this.commonDir = commonDir;
        this.testCaseDir = testCaseDir;
        this.testClassName = "testcases." + testName + ".Test";
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

        Compilation compilation = javac().withProcessors(
                new Processor(error -> this.error = error),
                new ComponentProcessor()).compile(files);

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
}
