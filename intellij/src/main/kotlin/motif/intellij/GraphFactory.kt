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
package motif.intellij

import com.intellij.lang.jvm.JvmModifier
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.InheritanceUtil
import motif.Scope
import motif.ScopeFactory
import motif.ast.IrClass
import motif.ast.intellij.IntelliJClass
import motif.core.ResolvedGraph
import kotlin.system.measureTimeMillis

class GraphFactory(private val project: Project) {

    private val psiManager = PsiManager.getInstance(project)
    private val psiElementFactory = PsiElementFactory.SERVICE.getInstance(project)

    fun compute(): ResolvedGraph {
        val motifClasses = { getMotifClasses() }.runAndLog("Found Motif classes.")
        val scopeClasses = motifClasses.filter { it.type == MotifClass.Type.SCOPE }.map { it.clazz }
        val scopeFactoryClasses = motifClasses.filter { it.type == MotifClass.Type.SCOPE_FACTORY }.map { it.clazz }
        return { ResolvedGraph.create(scopeClasses, scopeFactoryClasses) }.runAndLog("Processed graph.")
    }

    private fun getMotifClasses(): List<MotifClass> {
        val motifClasses = mutableListOf<MotifClass>()
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent { file ->
            motifClasses.addAll(getMotifClasses(file))
            true
        }
        return motifClasses
    }

    private fun getMotifClasses(virtualFile: VirtualFile): List<MotifClass> {
        val psiFile = psiManager.findFile(virtualFile) ?: return emptyList()
        val javaFile = psiFile as? PsiJavaFile ?: return emptyList()
        return javaFile.classes
                .mapNotNull {
                    val type = when {
                        isScopeClass(it) -> MotifClass.Type.SCOPE
                        isScopeFactoryClass(it) -> MotifClass.Type.SCOPE_FACTORY
                        else -> return@mapNotNull null
                    }
                    val psiClassType = psiElementFactory.createType(it)
                    val clazz = IntelliJClass(project, psiClassType)
                    MotifClass(clazz, type)
                }
    }

    private fun isScopeClass(psiClass: PsiClass): Boolean {
        return psiClass.annotations.find { it.qualifiedName == Scope::class.qualifiedName} != null
    }

    private fun isScopeFactoryClass(psiClass: PsiClass): Boolean {
        if (psiClass.hasModifier(JvmModifier.ABSTRACT)) return false
        return InheritanceUtil.isInheritor(psiClass, true, ScopeFactory::class.java.name)
    }

    private fun <T : Any> (() -> T).runAndLog(message: String): T {
        lateinit var result: T
        val duration = measureTimeMillis { result = this() }
        log("$message (${duration}ms)")
        return result
    }

    private fun log(message: String) {
        Notifications.Bus.notify(
                Notification("Motif", "Motif Graph", message, NotificationType.INFORMATION))
    }

    private data class MotifClass(val clazz: IntelliJClass, val type: Type) {
        enum class Type { SCOPE, SCOPE_FACTORY }
    }
}