package com.google.testing.compile;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class CompilationFactory {

    public static Compilation create(
            Compiler compiler,
            Iterable<? extends JavaFileObject> sourceFiles,
            boolean successful,
            Iterable<Diagnostic<? extends JavaFileObject>> diagnostics,
            Iterable<JavaFileObject> generatedFiles) {
        return new Compilation(compiler, sourceFiles, successful, diagnostics, generatedFiles);
    }
}
