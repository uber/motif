package com.uber.motif.compiler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TestHarness {

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        File testCaseRoot = new File("../it/src/main/java/testcases");
        File[] testCaseDirs = testCaseRoot.listFiles((dir, name) -> dir.isDirectory() && name.startsWith("T"));
        if (testCaseDirs == null) throw new IllegalStateException("Could not find test case directories: " + testCaseRoot);
        return Arrays.stream(testCaseDirs)
                .map(file -> new Object[]{file, file.getName()})
                .collect(Collectors.toList());
    }

    private final File testCaseDir;

    @SuppressWarnings("unused")
    public TestHarness(File testCaseDir, String ignore) {
        this.testCaseDir = testCaseDir;
    }

    @Test
    public void test() {
        
    }
}
