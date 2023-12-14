/*
 * Copyright (c) 2022 Uber Technologies, Inc.
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
package motif.compiler

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.XProcessingEnvConfig
import androidx.room.compiler.processing.util.Source
import androidx.room.compiler.processing.util.compiler.TestCompilationArguments
import androidx.room.compiler.processing.util.compiler.TestCompilationResult
import androidx.room.compiler.processing.util.compiler.compile
import com.google.common.collect.Lists.cartesianProduct
import com.google.common.truth.Truth
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import dagger.internal.codegen.ComponentProcessor
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader
import java.nio.charset.Charset
import java.nio.file.Files
import javax.tools.Diagnostic
import motif.compiler.ksp.MotifSymbolProcessorProvider
import motif.core.ResolvedGraph
import motif.errormessage.ErrorMessage.Companion.footer
import motif.errormessage.ErrorMessage.Companion.header
import motif.viewmodel.TestRenderer.render
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@ExperimentalProcessingApi
class TestHarness(
    private val processorType: ProcessorType,
    private val outputMode: OutputMode,
    private val testCaseDir: File,
    testName: String,
) {

  @Rule @JvmField var temporaryFolder = TemporaryFolder()

  private val externalDir = EXTERNAL_ROOT.resolve(testName)
  private val testClassName = "testcases.$testName.Test"
  private val errorFile = testCaseDir.resolve("ERROR.txt")
  private val graphFile = testCaseDir.resolve("GRAPH.txt")
  private val proguardFile = testCaseDir.resolve("config.pro")
  private val isErrorTest = testName.matches("^K?E.*".toRegex())

  companion object {
    private val SOURCE_ROOT = File("../tests/src/main/java/")
    private val TEST_CASE_ROOT = File(SOURCE_ROOT, "testcases")
    private val EXTERNAL_ROOT = File(SOURCE_ROOT, "external")

    // Used for debugging. Run all by default
    private val testFilter = { proc: ProcessorType, mode: OutputMode, dir: File, name: String ->
      true
    }

    @JvmStatic
    @Parameterized.Parameters(name = "{0}_{1}_{3}")
    fun data(): Collection<Array<Any>> {
      val testCaseDirs = TEST_CASE_ROOT.listFiles { file: File -> isTestDir(file) }
      val combos =
          cartesianProduct(
              ProcessorType.values().toList(), OutputMode.values().toList(), testCaseDirs.toList())
      return combos
          .filterNot { (_, mode, dir) ->
            mode == OutputMode.KOTLIN && (dir as File).resolve("SKIP_KOTLIN").exists()
          }
          .map { it + (it.last() as File).name }
          .filterNot { (proc, mode, _, _) ->
            // We don't generate Java from KSP
            proc == ProcessorType.KSP && mode == OutputMode.JAVA
          }
          .filterNot { (proc, _, _, name) ->
            // Can't run KSP on Java sources until fixed: https://github.com/google/ksp/issues/1086
            proc == ProcessorType.KSP && "$name"[0] in setOf('T', 'E')
          }
          .filter { (proc, mode, dir, name) ->
            testFilter(proc as ProcessorType, mode as OutputMode, dir as File, name as String)
          }
          .map { it.toTypedArray() as Array<Any> }
    }

    private fun isTestDir(file: File): Boolean {
      return file.isDirectory &&
          file.listFiles().isNotEmpty() &&
          file.name.matches("^K?[TE].*".toRegex())
    }
  }

  @Test
  fun test() {
    val externalClassesDirs = compileExternalDir()
    val (annotationProcessor, symbolProcessorProvider) = createProcessors()
    val result =
        compileDir(testCaseDir, annotationProcessor, symbolProcessorProvider, externalClassesDirs)
    val graph =
        when (processorType) {
          ProcessorType.AP -> annotationProcessor?.graph
          ProcessorType.KSP -> symbolProcessorProvider?.graph
        }
    checkNotNull(graph) { "No graph found during processing" }

    if (isErrorTest) {
      runErrorTest(result, graph)
    } else {
      assertSucceeded(result)

      val proguardedClasses =
          ProGuard.run(
              externalClassesDirs,
              result.outputClasspath.filterNot {
                "/ksp/" in it.absolutePath || "/kapt/" in it.absolutePath
              },
              proguardFile)

      val urls = (externalClassesDirs + proguardedClasses).map { it.toURI().toURL() }.toTypedArray()
      val classLoader: ClassLoader = URLClassLoader(urls, javaClass.classLoader)

      if (externalClassesDirs.isEmpty()) {
        URLClassLoader(arrayOf(proguardedClasses.toURI().toURL()), javaClass.classLoader)
      }
      val testClass = classLoader.loadClass(testClassName)

      if (testClass.getAnnotation(Ignore::class.java) == null) {
        runSuccessTest(testClass, graph)
      }
    }
  }

  private fun compileExternalDir(): List<File> {
    if (externalDir.exists()) {
      val externalDirContents = externalDir.listFiles()
      if (externalDirContents?.isNotEmpty() == true) {
        val (annotationProcessor, symbolProcessorProvider) = createProcessors()
        val shouldProcess = !File(externalDir, "DO_NOT_PROCESS").exists()
        val externalResult =
            compileDir(
                externalDir,
                if (shouldProcess) annotationProcessor else null,
                if (shouldProcess) symbolProcessorProvider else null,
                emptyList())
        assertSucceeded(externalResult)
        return externalResult.outputClasspath.filter { it.listFilesRecursively().isNotEmpty() }
      }
    }
    return emptyList()
  }

  private fun compileDir(
      sourcesDir: File,
      annotationProcessor: javax.annotation.processing.Processor?,
      symbolProcessorProvider: SymbolProcessorProvider?,
      classpath: List<File> = emptyList()
  ): TestCompilationResult {
    val processorOptions = mapOf("motif.mode" to outputMode.name.lowercase())
    val sources = getFiles(sourcesDir).asSources()
    val annotationProcessors =
        annotationProcessor?.let { listOf(it, ComponentProcessor()) } ?: emptyList()
    return compile(
        workingDir = Files.createTempDirectory("test-runner").toFile(),
        arguments =
            TestCompilationArguments(
                sources = sources,
                classpath = classpath,
                inheritClasspath = true,
                kaptProcessors = annotationProcessors,
                symbolProcessorProviders = symbolProcessorProvider?.let { listOf(it) }
                        ?: emptyList(),
                processorOptions = processorOptions,
            ))
  }

  private fun List<File>.asSources(): List<Source> {
    return mapNotNull { file ->
      val relPath = file.relativeTo(File("../tests/src/main/java")).toString()
      val qName = relPath.substringBeforeLast(".").replace('/', '.')
      return@mapNotNull when (file.extension) {
        "java" -> Source.loadJavaSource(file, qName)
        "kt" -> Source.loadKotlinSource(file, relPath)
        else -> null
      }
    }
  }

  private fun getFiles(dir: File): List<File> {
    return dir.walkTopDown()
        .filter {
          !it.isDirectory && it.extension in setOf("kt", "java") && it.name != "ScopeImpl.java"
        }
        .toList()
  }

  private fun createProcessors(): Pair<Processor?, MotifSymbolProcessorProvider?> {
    val xProcConfig = XProcessingEnvConfig.DEFAULT.copy(false, true)
    val annotationProcessor =
        when (processorType) {
          ProcessorType.AP -> Processor()
          ProcessorType.KSP -> null
        }
    val symbolProcessorProvider =
        when (processorType) {
          ProcessorType.AP -> null
          ProcessorType.KSP -> MotifSymbolProcessorProvider(xProcConfig)
        }
    return annotationProcessor to symbolProcessorProvider
  }

  private fun assertSucceeded(result: TestCompilationResult) {
    if (!result.success || result.diagnostics[Diagnostic.Kind.ERROR].orEmpty().isNotEmpty()) {
      Truth.assertWithMessage(result.messages()).fail()
    }
  }

  private fun assertFailed(result: TestCompilationResult) {
    if (result.success) {
      Truth.assertWithMessage("Expected compilation to fail but encountered no errors.").fail()
    }
  }

  @Throws(Throwable::class)
  private fun runErrorTest(result: TestCompilationResult, graph: ResolvedGraph) {
    assertFailed(result)
    val expectedErrorString = getExistingErrorString()
    val actualErrorString = getActualErrorString(result)
    if (expectedErrorString != actualErrorString) {
      BufferedWriter(FileWriter(errorFile)).use { out -> out.write(actualErrorString) }
      Truth.assertWithMessage(
              """
        Error message has changed. The ERROR.txt file has been automatically updated by this test:
          1. Verify that the changes are correct.
          2. Commit the changes to source control.
        """.trimIndent())
          .that(actualErrorString)
          .isEqualTo(expectedErrorString)
    }
    runGraphTest(graph)
  }

  @Throws(Throwable::class)
  private fun runSuccessTest(testClass: Class<*>, graph: ResolvedGraph) {
    try {
      testClass.getMethod("run").invoke(null)
    } catch (e: InvocationTargetException) {
      throw e.cause!!
    }
    runGraphTest(graph)
  }

  @Throws(Throwable::class)
  private fun runGraphTest(graph: ResolvedGraph) {
    val expectedGraphString = getExistingGraphString()
    val actualGraphString = getActualGraphString(graph)
    if (expectedGraphString != actualGraphString) {
      BufferedWriter(FileWriter(graphFile)).use { out -> out.write(actualGraphString) }
      Truth.assertWithMessage(
              """
        Graph representation has changed. The GRAPH.txt file has been automatically updated by this test:
          1. Verify that the changes are correct.
          2. Commit the changes to source control.
        """.trimIndent())
          .fail()
    }
  }

  private fun getActualGraphString(graph: ResolvedGraph): String {
    val message = render(graph)
    val header =
        """
      ########################################################################
      #                                                                      #
      # This file is auto-generated by running the Motif compiler tests and  #
      # serves a as validation of graph correctness. IntelliJ plugin tests   #
      # also rely on this file to ensure that the plugin graph understanding #
      # is equivalent to the compiler's.                                     #
      #                                                                      #
      # - Do not edit manually.                                              #
      # - Commit changes to source control.                                  #
      # - Since this file is autogenerated, code review changes carefully to #
      #   ensure correctness.                                                #
      #                                                                      #
      ########################################################################

      """.trimIndent()
    return "$header\n$message\n"
  }

  @Throws(IOException::class)
  private fun getExistingGraphString(): String {
    return if (graphFile.exists()) {
      com.google.common.io.Files.asCharSource(graphFile, Charset.defaultCharset()).read()
    } else {
      ""
    }
  }

  private fun getActualErrorString(result: TestCompilationResult): String {
    val message = getMessage(result)
    val header =
        """
      ########################################################################
      #                                                                      #
      # This file is auto-generated by running the Motif compiler tests and  #
      # serves both as validation of error correctness and as a record of    #
      # the current compiler error output.                                   #
      #                                                                      #
      # - Do not edit manually.                                              #
      # - Commit changes to source control.                                  #
      # - Since this file is autogenerated, code review changes carefully to #
      #   ensure correctness.                                                #
      #                                                                      #
      ########################################################################

      """.trimIndent()
    return "$header\n$message\n"
  }

  private fun getMessage(result: TestCompilationResult): String {
    val header = toCompilerMessage(header).trimIndent()
    val footer = toCompilerMessage(footer).trimIndent()
    val resultMessage =
        result.messages().trimIndent().substringAfter(header).substringBefore(footer)
    return "$header$resultMessage$footer".prependIndent("  ")
  }

  private fun toCompilerMessage(message: String): String {
    return message.trim().prependIndent("  ")
  }

  @Throws(IOException::class)
  private fun getExistingErrorString(): String {
    return if (errorFile.exists()) {
      com.google.common.io.Files.asCharSource(errorFile, Charset.defaultCharset()).read()
    } else {
      ""
    }
  }
}

internal fun File.listFilesRecursively() = walkTopDown().filter { it.isFile }.toList()

private fun TestCompilationResult.messages(): String {
  return diagnostics[Diagnostic.Kind.ERROR].orEmpty().flatMap { it.msg.lines() }.joinToString(
      separator = "\n") { "  $it" }
}
