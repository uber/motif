/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
    private final File humanReadableFile;
    private final boolean isErrorTest;

    private File externalOutputDir;

    @SuppressWarnings("unused")
    public TestHarness(File testCaseDir, File externalDir, String testName) {
        this.testCaseDir = testCaseDir;
        this.externalDir = externalDir;
        this.testClassName = "testcases." + testName + ".Test";
        this.humanReadableFile = new File(testCaseDir, "ERROR.txt");
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
                    externalDir);
            assertThat(externalCompilation).succeeded();
        }

        Processor processor = new Processor();
        Compilation compilation = compiler.compile(
                externalOutputDir,
                null,
                processor,
                testCaseDir);

        if (isErrorTest) {
            runErrorTest(compilation, processor.graph);
        } else {
            runSuccessTest(compilation);
        }
    }

    private void runErrorTest(Compilation compilation, ResolvedGraph graph) throws IOException {
        assertThat(compilation).failed();

        List<Diagnostic<? extends JavaFileObject>> diagnostics = compilation.diagnostics().asList();
        Truth.assertThat(diagnostics).hasSize(1);
        Diagnostic diagnostic = diagnostics.get(0);

        String expectedHumanReadableString = getExistingHumanReadableString();
        String actualHumanReadableString = getActualHumanReadableString(diagnostic);

        if (!expectedHumanReadableString.equals(actualHumanReadableString)) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(humanReadableFile))) {
                out.write(actualHumanReadableString);
            }
            Truth.assertWithMessage("Error message has changed. The ERROR.txt file has been " +
                    "automatically updated by this test:\n" +
                    "  1. Verify that the changes are correct.\n" +
                    "  2. Commit the changes to source control.\n").fail();
        }
    }

    private void runSuccessTest(Compilation compilation) throws Throwable {
        assertThat(compilation).succeeded();
        Class<?> testClass = loadTestClass(compilation);

        if (testClass.getAnnotation(Ignore.class) != null) {
            return;
        }

        assertThat(compilation).succeeded();

        try {
            testClass.getField("loadedClasses");
            // Skip buck loaded classes test. No longer supported.
            // TODO Remove T031 after old compiler is removed.
            return;
        } catch (NoSuchFieldException ignore) {}

        try {
            testClass.getMethod("run").invoke(null);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private String getActualHumanReadableString(Diagnostic diagnostic) {
        String message = diagnostic.getMessage(Locale.getDefault());
        String header =
                "########################################################################\n" +
                "#                                                                      #\n" +
                "# This file is auto-generated by running the Motif compiler tests and  #\n" +
                "# serves both as validation of error correctness and as a record of    #\n" +
                "# the current compiler error output.                                   #\n" +
                "#                                                                      #\n" +
                "# - Do not edit manually.                                              #\n" +
                "# - Commit changes to source control.                                  #\n" +
                "# - Since this file is autogenerated, code review changes carefully to #\n" +
                "#   ensure correctness.                                                #\n" +
                "#                                                                      #\n" +
                "########################################################################\n";
        return header + message + "\n";
    }

    private String getExistingHumanReadableString() throws IOException {
        if (humanReadableFile.exists()) {
            return Files.asCharSource(humanReadableFile, Charset.defaultCharset()).read();
        } else {
            return "";
        }
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
