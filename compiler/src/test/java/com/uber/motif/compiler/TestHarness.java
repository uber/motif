package com.uber.motif.compiler;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import dagger.internal.codegen.ComponentProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

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

    @SuppressWarnings("unused")
    public TestHarness(File testCaseDir, String ignore) {
        this.testCaseDir = testCaseDir;
    }

    @Test
    public void test() throws IOException {
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

        assertThat(compilation).succeeded();
    }
}
