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
import com.tschuchort.compiletesting.KotlinCompilation;
import com.tschuchort.compiletesting.SourceFile;
import dagger.internal.codegen.ComponentProcessor;
import kotlin.text.StringsKt;
import motif.core.ResolvedGraph;
import motif.errormessage.ErrorMessage;
import motif.viewmodel.TestRenderer;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertWithMessage;

@RunWith(Parameterized.class)
public class TestHarness {

    private static final File SOURCE_ROOT = new File("../tests/src/main/java/");
    private static final File TEST_CASE_ROOT = new File(SOURCE_ROOT, "testcases");
    private static final File EXTERNAL_ROOT = new File(SOURCE_ROOT, "external");

    private static class TestParameters {

        final File testDirectory;
        final Mode mode;

        public TestParameters(File testDirectory, Mode mode) {
            this.testDirectory = testDirectory;
            this.mode = mode;
        }
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        File[] testCaseDirs = TEST_CASE_ROOT.listFiles(TestHarness::isTestDir);
        if (testCaseDirs == null) throw new IllegalStateException("Could not find test case directories: " + TEST_CASE_ROOT);
        return Arrays.stream(Mode.values())
                .flatMap(mode -> Arrays.stream(testCaseDirs).map(file -> new TestParameters(file, mode)))
                .filter(parameters -> !skipTest(parameters.mode, parameters.testDirectory))
                .map(parameters -> {
                    String displayName = parameters.testDirectory.getName() + "_" + parameters.mode;
                    return new Object[]{parameters, displayName};
                })
                .collect(Collectors.toList());
    }

    private static boolean skipTest(Mode mode, File testDirectory) {
        if (mode == Mode.KOTLIN && skipKotlin(testDirectory)) {
            return true;
        }
        return false;
    }

    private static boolean skipKotlin(File testDirectory) {
        return new File(testDirectory, "SKIP_KOTLIN").exists();
    }

    private static boolean isTestDir(File file) {
        String filename = file.getName();
        return file.isDirectory()
                && file.listFiles().length > 0
                && (filename.startsWith("T") || filename.startsWith("E"));
    }

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final Mode mode;
    private final File testCaseDir;
    private final File externalDir;
    private final String testClassName;
    private final File errorFile;
    private final File graphFile;
    private final File proguardFile;
    private final boolean isErrorTest;

    @SuppressWarnings("unused")
    public TestHarness(TestParameters parameters, String displayName) {
        String testName = parameters.testDirectory.getName();
        this.mode = parameters.mode;
        this.testCaseDir = new File(TEST_CASE_ROOT, testName);
        this.externalDir = new File(EXTERNAL_ROOT, testName);
        this.testClassName = "testcases." + testName + ".Test";
        this.errorFile = new File(testCaseDir, "ERROR.txt");
        this.graphFile = new File(testCaseDir, "GRAPH.txt");
        this.proguardFile = new File(testCaseDir, "config.pro");
        this.isErrorTest = testName.startsWith("E");
    }

    @Test
    public void test() throws Throwable {
        Map<String, String> options = new HashMap<>();
        options.put("motif.mode", mode.name().toLowerCase());

        File externalClassesDir = null;
        File[] externalDirContents = externalDir.listFiles();
        if (externalDirContents != null && externalDirContents.length > 0) {
            boolean shouldProcess = !new File(externalDir, "DO_NOT_PROCESS").exists();
            KotlinCompilation.Result externalResult = compile(
                    getFiles(externalDir),
                    Collections.emptyList(),
                    options,
                    shouldProcess ? new Processor() : null);
            assertSucceeded(externalResult);
            externalClassesDir = externalResult.getOutputDirectory();
        }

        List<File> classpaths = externalClassesDir == null
                ? Collections.emptyList()
                : Collections.singletonList(externalClassesDir);
        Processor processor = new Processor();

        KotlinCompilation.Result result = compile(
                getFiles(testCaseDir),
                classpaths,
                options,
                processor);

        if (isErrorTest) {
            runErrorTest(result);
        } else {
            assertSucceeded(result);

            File proguardedClasses = ProGuard.run(
                    externalClassesDir,
                    result.getOutputDirectory(),
                    proguardFile);

            ClassLoader classLoader;
            if (externalClassesDir == null) {
                classLoader = new URLClassLoader(
                        new URL[]{
                                proguardedClasses.toURI().toURL()
                        },
                        getClass().getClassLoader());
            } else {
                classLoader = new URLClassLoader(
                        new URL[]{
                                proguardedClasses.toURI().toURL(),
                                externalClassesDir.toURI().toURL()
                        }, getClass().getClassLoader());
            }
            Class<?> testClass = classLoader.loadClass(testClassName);

            if (testClass.getAnnotation(Ignore.class) == null) {
                runSuccessTest(testClass, processor.graph);
            }
        }
    }

    private void assertSucceeded(KotlinCompilation.Result result) {
        if (result.getExitCode() != KotlinCompilation.ExitCode.OK) {
            assertWithMessage(result.getMessages()).fail();
        }
    }

    private void assertFailed(KotlinCompilation.Result result) {
        if (result.getExitCode() == KotlinCompilation.ExitCode.OK) {
            assertWithMessage("Expected compilation to fail but encountered no errors.").fail();
        }
    }

    private KotlinCompilation.Result compile(
            List<File> sourceFiles,
            List<File> classpaths,
            Map<String, String> aptArgs,
            @Nullable Processor motifProcessor) {
        KotlinCompilation compilation = new KotlinCompilation();
        compilation.setAnnotationProcessors(getProcessors(motifProcessor));
        compilation.setSources(sourceFiles.stream()
                .map(SourceFile.Companion::fromPath)
                .collect(Collectors.toList()));
        compilation.setInheritClassPath(true);
        compilation.setClasspaths(classpaths);
        compilation.setKaptArgs(aptArgs);
        compilation.setVerbose(false);

        return compilation.compile();
    }

    private List<File> getFiles(File dir) throws IOException {
        return getFiles(dir, file -> (file.getName().endsWith(".java") || file.getName().endsWith(".kt")) && !file.getName().equals("ScopeImpl.java"));
    }

    private List<File> getFiles(File dir, Predicate<File> filter) throws IOException {
        return java.nio.file.Files.walk(dir.toPath())
                .map(Path::toFile)
                .filter(file -> !file.isDirectory() && filter.test(file))
                .collect(Collectors.toList());
    }

    private List<javax.annotation.processing.Processor> getProcessors(@Nullable Processor motifProcessor) {
        List<javax.annotation.processing.Processor> processors = new ArrayList<>();
        processors.add(new ComponentProcessor());
        if (motifProcessor != null) {
            processors.add(motifProcessor);
        }
        return processors;
    }

    private void runErrorTest(KotlinCompilation.Result result) throws IOException {
        assertFailed(result);

        String expectedErrorString = getExistingErrorString();
        String actualErrorString = getActualErrorString(result);

        if (!expectedErrorString.equals(actualErrorString)) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(errorFile))) {
                out.write(actualErrorString);
            }
            assertWithMessage("Error message has changed. The ERROR.txt file has been " +
                    "automatically updated by this test:\n" +
                    "  1. Verify that the changes are correct.\n" +
                    "  2. Commit the changes to source control.\n").fail();
        }
    }

    private void runSuccessTest(Class<?> testClass, ResolvedGraph graph) throws Throwable {
        try {
            testClass.getMethod("run").invoke(null);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }

        String expectedGraphString = getExistingGraphString();
        String actualGraphString = getActualGraphString(graph);

        if (!expectedGraphString.equals(actualGraphString)) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(graphFile))) {
                out.write(actualGraphString);
            }
            assertWithMessage("Graph representation has changed. The GRAPH.txt file has been " +
                    "automatically updated by this test:\n" +
                    "  1. Verify that the changes are correct.\n" +
                    "  2. Commit the changes to source control.\n").fail();
        }
    }

    private String getActualGraphString(ResolvedGraph graph) {
        String message = TestRenderer.render(graph);
        String header =
                "########################################################################\n" +
                "#                                                                      #\n" +
                "# This file is auto-generated by running the Motif compiler tests and  #\n" +
                "# serves a as validation of graph correctness. IntelliJ plugin tests   #\n" +
                "# also rely on this file to ensure that the plugin graph understanding #\n" +
                "# is equivalent to the compiler's.                                     #\n" +
                "#                                                                      #\n" +
                "# - Do not edit manually.                                              #\n" +
                "# - Commit changes to source control.                                  #\n" +
                "# - Since this file is autogenerated, code review changes carefully to #\n" +
                "#   ensure correctness.                                                #\n" +
                "#                                                                      #\n" +
                "########################################################################\n";
        return header + "\n" + message + "\n";
    }

    private String getExistingGraphString() throws IOException {
        if (graphFile.exists()) {
            return Files.asCharSource(graphFile, Charset.defaultCharset()).read();
        } else {
            return "";
        }
    }

    private String getActualErrorString(KotlinCompilation.Result result) {
        String message = getMessage(result);
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

    private String getMessage(KotlinCompilation.Result result) {
        String resultMessage = result.getMessages();
        String header = toCompilerMessage(ErrorMessage.Companion.getHeader());
        String footer = toCompilerMessage(ErrorMessage.Companion.getFooter());
        resultMessage = StringsKt.substringAfter(resultMessage, header, resultMessage);
        resultMessage = StringsKt.substringBefore(resultMessage, footer, resultMessage);
        return "\n" + header + resultMessage + footer;
    }

    private String toCompilerMessage(String message) {
        return StringsKt.prependIndent(message.trim(), "  ");
    }

    private String getExistingErrorString() throws IOException {
        if (errorFile.exists()) {
            return Files.asCharSource(errorFile, Charset.defaultCharset()).read();
        } else {
            return "";
        }
    }
}
