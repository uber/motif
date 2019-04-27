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
package motif.compiler;

import com.google.common.io.Files;
import com.google.common.truth.Truth;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.MotifTestCompiler;
import motif.core.ResolvedGraph;
import motif.models.MotifError;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.google.testing.compile.CompilationSubject.assertThat;

@RunWith(Parameterized.class)
public class TestHarness {

    @Parameterized.Parameters(name = "{2}")
    public static Collection<Object[]> data() {
        File sourceRoot = new File("../tests/src/main/java/");
        File testCaseRoot = new File(sourceRoot, "testcases");
        File externalRoot = new File(sourceRoot, "external");
        File[] testCaseDirs = testCaseRoot.listFiles(TestHarness::isTestDir);
        if (testCaseDirs == null) throw new IllegalStateException("Could not find test case directories: " + testCaseRoot);
        return Arrays.stream(testCaseDirs)
                .map(file -> {
                    File externalDir = new File(externalRoot, file.getName());
                    return new Object[]{file.getAbsoluteFile(), externalDir, file.getName()};
                })
                .collect(Collectors.toList());
    }

    private static boolean isTestDir(File file) {
        String filename = file.getName();
        return file.isDirectory()
                && file.listFiles().length > 0
                && (filename.startsWith("T") || filename.startsWith("E"));
    }

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final MotifTestCompiler compiler = new MotifTestCompiler();

    private final File testCaseDir;
    private final File externalDir;
    private final String testClassName;
    private final File errorFile;
    private final File humanReadableFile;
    private final boolean isErrorTest;

    private File externalOutputDir;

    @SuppressWarnings("unused")
    public TestHarness(File testCaseDir, File externalDir, String testName) {
        this.testCaseDir = testCaseDir;
        this.externalDir = externalDir;
        this.testClassName = "testcases." + testName + ".Test";
        this.errorFile = new File(testCaseDir, "ERROR.txt");
        this.humanReadableFile = new File(testCaseDir, "HUMAN.txt");
        this.isErrorTest = testName.startsWith("E");
    }

    @Test
    public void test() throws Throwable {
        File[] externalDirContents = externalDir.listFiles();
        boolean hasExternalSources = externalDirContents != null && externalDirContents.length > 0;
        externalOutputDir = hasExternalSources ? temporaryFolder.newFolder() : null;

        if (externalOutputDir != null) {
            boolean shouldProcess = !new File(externalDir, "DO_NOT_PROCESS").exists();
            Compilation externalCompilation = compiler.compile(
                    null,
                    externalOutputDir,
                    shouldProcess ? new Processor() : null,
                    externalDir).compilation;
            assertThat(externalCompilation).succeeded();
        }

        Processor processor = new Processor();
        MotifTestCompiler.Result result = compiler.compile(
                externalOutputDir,
                null,
                processor,
                testCaseDir);
        Compilation compilation = result.compilation;

        if (isErrorTest) {
            runErrorTest(compilation, processor.graph);
        } else {
            runSuccessTest(result);
        }
    }

    private void runErrorTest(Compilation compilation, ResolvedGraph graph) throws IOException {
        assertThat(compilation).failed();
        Truth.assertWithMessage("ERROR.txt file does not exist")
                .that(errorFile.exists()).
                isTrue();
        String expectedErrorString = Files.asCharSource(errorFile, Charset.defaultCharset()).read();
        String actualErrorString = getErrorString(graph);
        Truth.assertThat(expectedErrorString).isEqualTo(actualErrorString);
    }

    private void runSuccessTest(MotifTestCompiler.Result result) throws Throwable {
        assertThat(result.compilation).succeeded();
        Class<?> testClass = loadTestClass(result.compilation);

        if (testClass.getAnnotation(Ignore.class) != null) {
            return;
        }

        assertThat(result.compilation).succeeded();

        try {
            Field loadedClasses = testClass.getField("loadedClasses");
            loadedClasses.set(null, result.classnames);
        } catch (NoSuchFieldException ignore) {}

        try {
            testClass.getMethod("run").invoke(null);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private String getErrorString(ResolvedGraph graph) {
        StringJoiner joiner = new StringJoiner("\n");
        for (MotifError error : graph.getErrors()) {
            String errorString = error.getClass().getSimpleName() + ": " + error.getTestString();
            joiner.add(errorString);
        }
        return joiner.toString();
    }

    private Class<?> loadTestClass(Compilation compilation) throws ClassNotFoundException, MalformedURLException {
        ClassLoader classLoader;
        if (externalOutputDir == null) {
            classLoader = new CompilationClassLoader(compilation);
        } else {
            ClassLoader externalClassLoader = new URLClassLoader(new URL[]{externalOutputDir.toURI().toURL()});
            classLoader = new CompilationClassLoader(externalClassLoader, compilation);
        }
        return classLoader.loadClass(testClassName);
    }
}
