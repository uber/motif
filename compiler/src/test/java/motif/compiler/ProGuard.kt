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
package motif.compiler

import com.google.common.truth.Truth
import dagger.Component
import motif.Scope
import org.jetbrains.annotations.NotNull
import proguard.ClassPath
import proguard.ClassPathEntry
import proguard.Configuration
import proguard.ConfigurationParser
import java.io.File
import javax.annotation.Nullable
import javax.inject.Inject
import kotlin.reflect.KClass

object ProGuard {

    private val classPathFiles: List<File> by lazy {
        val rtJar = File(System.getProperty("java.home"), "lib/rt.jar");
        listOf(Scope::class,
                Truth::class,
                Inject::class,
                Nullable::class,
                Component::class,
                Unit::class,
                NotNull::class)
                .map { libraryPath(it) } + rtJar
    }

    @JvmStatic
    fun run(
            externalClassesDir: File?,
            classesDir: File,
            proguardFile: File): File {
        val outputDir = createTempDir()
        val outputJar = outputDir.resolve("proguarded.jar")

        val config = Configuration().apply {
            programJars = ClassPath().apply {
                add(ClassPathEntry(classesDir, false))
                add(ClassPathEntry(outputJar, true))
            }
            libraryJars = ClassPath().apply {
                (listOfNotNull(externalClassesDir) + classPathFiles)
                        .forEach { add(ClassPathEntry(it, false)) }
            }
        }

        val configURL = if (proguardFile.exists()) {
            proguardFile.toURI().toURL()
        } else {
            this::class.java.getResource("/default.pro")
        }
        ConfigurationParser(configURL, System.getProperties()).parse(config)
        proguard.ProGuard(config).execute()
        return outputJar
    }

    private fun libraryPath(clazz: KClass<*>): File {
        return File(clazz.java.protectionDomain.codeSource.location.toURI())
    }
}