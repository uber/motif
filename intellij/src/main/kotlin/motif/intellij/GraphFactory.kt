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
import motif.Scope
import motif.ast.IrClass
import motif.ast.intellij.IntelliJClass
import motif.core.ResolvedGraph
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.toUElementOfType
import kotlin.system.measureTimeMillis

class GraphFactory(private val project: Project) {

    private val psiManager = PsiManager.getInstance(project)
    private val psiElementFactory = PsiElementFactory.SERVICE.getInstance(project)

    fun compute(): ResolvedGraph {
        val scopeClasses = { getScopeClasses() }.runAndLog("Found Scope classes.")
        return { ResolvedGraph.create(scopeClasses) }.runAndLog("Processed graph.")
    }

    private fun getScopeClasses(): List<IrClass> {
        val scopeClasses = mutableListOf<IrClass>()
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent { file ->
            scopeClasses.addAll(getScopeClasses(file))
            true
        }
        return scopeClasses
    }

    private fun getScopeClasses(virtualFile: VirtualFile): List<IrClass> {
        val psiFile = psiManager.findFile(virtualFile) ?: return emptyList()
        val psiClasses: Iterable<PsiClass> = when (psiFile) {
            is PsiJavaFile -> getScopeClasses(psiFile)
            is KtFile -> getScopeClasses(psiFile)
            else -> return emptyList()
        }
        return psiClasses
                .flatMap(this::getClasses)
                .filter(this::isScopeClass)
                .map(psiElementFactory::createType)
                .map { type ->
                    IntelliJClass(project, type)
                }
    }

    private fun getScopeClasses(psiFile: PsiJavaFile): Iterable<PsiClass> {
        return psiFile.classes.toList()
    }

    private fun getScopeClasses(psiFile: KtFile): Iterable<PsiClass> {
        return psiFile.declarations.filterIsInstance<KtClass>().map { it.toUElement(UClass::class.java)!!.javaPsi }
    }

    private fun getClasses(psiClass: PsiClass): List<PsiClass> {
        return listOf(psiClass) + psiClass.allInnerClasses.flatMap(this::getClasses)
    }

    private fun isScopeClass(psiClass: PsiClass): Boolean {
        return psiClass.annotations.find { it.qualifiedName == Scope::class.qualifiedName} != null
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
}