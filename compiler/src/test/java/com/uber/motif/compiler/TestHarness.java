package com.uber.motif.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.truth.Truth;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import dagger.internal.codegen.ComponentProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

@RunWith(Parameterized.class)
public class TestHarness {

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        File testCaseRoot = new File("../it/src/main/java/testcases");
        File[] testCaseDirs = testCaseRoot.listFiles((dir, name) -> dir.isDirectory() && name.startsWith("T"));
        if (testCaseDirs == null) throw new IllegalStateException("Could not find test case directories: " + testCaseRoot);
        return Arrays.stream(testCaseDirs)
                .map(file -> new Object[]{file.getAbsoluteFile(), file.getName()})
                .collect(Collectors.toList());
    }

    private final File testCaseDir;
    private final String testName;
    private final File errorFile;
    private final File outputFile;
    private final String testClassName;

    @SuppressWarnings("unused")
    public TestHarness(File testCaseDir, String testName) {
        this.testCaseDir = testCaseDir;
        this.testName = testName;
        this.errorFile = new File(testCaseDir, "ERROR.txt");
        this.outputFile = new File(testCaseDir, "OUTPUT.txt");
        this.testClassName = "testcases." + testName + ".Test";
    }

    @Test
    public void test() throws Throwable {
        Truth.assertThat(outputFile.exists());

        JavaFileObject[] files = Files.walk(testCaseDir.toPath())
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
                new AnnotationProcessor(),
                new ComponentProcessor()).compile(files);

        if (errorFile.exists()) {
            String expectedErrorString = readString(errorFile);

            ImmutableList<Diagnostic<? extends JavaFileObject>> errors = compilation.errors();
            Truth.assertThat(errors).hasSize(1);

            Diagnostic<? extends JavaFileObject> error = errors.iterator().next();

            Truth.assertThat(error.getMessage(Locale.getDefault())).isEqualTo(expectedErrorString);
        } else {
            assertThat(compilation).succeeded();
        }


        ClassLoader classLoader = new CompilationClassLoader(compilation);
        Class<?> testClass = classLoader.loadClass(testClassName);
        try {
            testClass.getMethod("run").invoke(null);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static String readString(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
}
