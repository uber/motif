/*
 * Copyright (c) 2025 Uber Technologies, Inc.
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
import androidx.room.compiler.processing.util.Source
import androidx.room.compiler.processing.util.compiler.TestCompilationArguments
import androidx.room.compiler.processing.util.compiler.compile
import com.google.common.truth.Truth
import dagger.internal.codegen.ComponentProcessor
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import javax.tools.Diagnostic
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Verifies that BASELINE generates byte-for-byte identical code to pre-change output.
 * BASELINE is the control arm of the caching-strategy A/B test.
 *
 * On mismatch the expected file is rewritten; verify the change is intended, then commit it.
 */
@RunWith(Parameterized::class)
@ExperimentalProcessingApi
class BaselineCodegenTest(
    private val caseName: String,
    private val mode: OutputMode,
) {

  companion object {
    private val SOURCE_ROOT = File("../tests/src/main/java")
    // Expected files under baseline_codegen/ were captured once from the pre-change main branch.
    private val EXPECTED_ROOT = File("src/test/resources/baseline_codegen")

    // Covers distinct BASELINE code shapes: no cache, cached dep,
    // @DoNotCache, spread, child scopes, interface Objects, static Objects methods.
    private val CASES =
        listOf(
            "T003_multiple_dependencies",
            "T009_dependency_cache",
            "T010_dependency_cache_donotcache",
            "T017_spread",
            "T024_child",
            "T021_objects_interface",
            "T044_static_objects_method",
        )

    @JvmStatic
    @Parameterized.Parameters(name = "{0}_{1}")
    fun data(): Collection<Array<Any>> =
        CASES.flatMap { case ->
          OutputMode.values().map { mode -> arrayOf<Any>(case, mode) }
        }
  }

  @Test
  fun baselineCodegenMatchesExpected() {
    // given: a scope compiled with the default BASELINE strategy
    val generated = generateScopeImpls(caseName, mode)
    Truth.assertWithMessage("No ScopeImpl generated for $caseName ($mode)")
        .that(generated)
        .isNotEmpty()

    // when: comparing generated output against expected files
    val expectedDir = File(EXPECTED_ROOT, "$caseName/${mode.name}")
    val mismatches = mutableListOf<String>()
    generated.forEach { (fileName, actual) ->
      val expectedFile = File(expectedDir, "$fileName.txt")
      val expected =
          if (expectedFile.exists()) {
            com.google.common.io.Files.asCharSource(expectedFile, Charset.defaultCharset()).read()
          } else {
            ""
          }
      if (expected != actual) {
        expectedFile.parentFile.mkdirs()
        expectedFile.writeText(actual)
        mismatches += "$caseName/${mode.name}/$fileName"
      }
    }

    // then: no drift from pre-change output
    if (mismatches.isNotEmpty()) {
      Truth.assertWithMessage(
              """
        BASELINE generated code changed for: ${mismatches.joinToString()}.
        BASELINE is the control arm and must match pre-change output.
          1. Verify the change is intended (this often signals accidental drift).
          2. Commit the updated expected file(s).
        """
                  .trimIndent(),
          )
          .fail()
    }
  }

  private fun generateScopeImpls(caseName: String, mode: OutputMode): Map<String, String> {
    val caseDir = File(SOURCE_ROOT, "testcases/$caseName")
    val sources = caseDir.asSources()
    val workingDir = Files.createTempDirectory("baseline-codegen-$caseName-$mode").toFile()
    val result =
        compile(
            workingDir = workingDir,
            arguments =
                TestCompilationArguments(
                    sources = sources,
                    classpath = emptyList(),
                    inheritClasspath = true,
                    kaptProcessors = listOf(Processor(), ComponentProcessor()),
                    kotlincArguments = listOf("-language-version", "1.9", "-api-version", "1.9"),
                    processorOptions = mapOf("motif.mode" to mode.name.lowercase()),
                ),
        )
    if (!result.success || result.diagnostics[Diagnostic.Kind.ERROR].orEmpty().isNotEmpty()) {
      val messages =
          result.diagnostics[Diagnostic.Kind.ERROR].orEmpty().joinToString("\n") { it.msg }
      Truth.assertWithMessage("Compilation failed for $caseName ($mode):\n$messages").fail()
    }
    val ext = if (mode == OutputMode.JAVA) "java" else "kt"
    return workingDir
        .walkTopDown()
        .filter { it.isFile && it.name.endsWith("Impl.$ext") }
        .associate { it.name to it.readText() }
        .toSortedMap()
  }

  private fun File.asSources(): List<Source> =
      walkTopDown()
          .filter {
            !it.isDirectory &&
                it.extension in setOf("kt", "java") &&
                it.name != "ScopeImpl.java" &&
                it.name != "Test.java"
          }
          .mapNotNull { file ->
            val relPath = file.relativeTo(SOURCE_ROOT).toString()
            val qName = relPath.substringBeforeLast(".").replace('/', '.')
            when (file.extension) {
              "java" -> Source.loadJavaSource(file, qName)
              "kt" -> Source.loadKotlinSource(file, relPath)
              else -> null
            }
          }
          .toList()
}
