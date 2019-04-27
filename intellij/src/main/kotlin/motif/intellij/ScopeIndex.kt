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
package motif.intellij

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.JavaFileElementType
import com.intellij.psi.search.ProjectScopeBuilder
import com.intellij.util.indexing.*
import com.intellij.util.io.BooleanDataDescriptor
import java.util.Random

/**
 * Invoked during IntelliJ's indexing phase. The DataIndexer marks which files contain a Motif Scope. Once indexed,
 * other parts of the plugin can retrieve all Scope files via ScopeIndex.getScopeClasses().
 */
class ScopeIndex : ScalarIndexExtension<Boolean>(), PsiDependentIndex {

    override fun getIndexer() = DataIndexer<Boolean, Void?, FileContent> { fileContent ->
        val isScopeFile = fileContent.psiFile.isMaybeScopeFile()
        mapOf(isScopeFile to null)
    }

    override fun getInputFilter() = object : DefaultFileTypeSpecificInputFilter(JavaFileType.INSTANCE) {

        override fun acceptInput(file: VirtualFile): Boolean {
            // All of the source code in the project. Would need to process more if we want to support Scopes provided
            // as a jar dependency.
            return JavaFileElementType.isInSourceContent(file)
        }
    }

    override fun getVersion() = Random().nextInt()
    override fun getName() = ID
    override fun dependsOnFileContent() = true
    override fun getKeyDescriptor() = BooleanDataDescriptor.INSTANCE!!

    fun getScopeClasses(project: Project): List<PsiClass> {
        val projectScopeBuilder = ProjectScopeBuilder.getInstance(project)
        val files: MutableList<VirtualFile> = mutableListOf()
        FileBasedIndex.getInstance().getFilesWithKey(ID, setOf(true), {
            files.add(it)
            true
        }, projectScopeBuilder.buildProjectScope())
        return files
                .mapNotNull { PsiManager.getInstance(project).findFile(it) }
                .flatMap(PsiFile::getScopeClasses)
    }

    companion object {

        private val ID: ID<Boolean, Void> = com.intellij.util.indexing.ID.create("ScopeIndex")

        fun getInstance(): ScopeIndex {
            return FileBasedIndexExtension.EXTENSION_POINT_NAME.findExtension(ScopeIndex::class.java)
                    ?: throw IllegalStateException("Could not find ScopeIndex. Make sure it is registered as a " +
                            "<fileBasedIndex> extension.")
        }
    }
}