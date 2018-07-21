package com.uber.motif.intellij.index

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.source.JavaFileElementType
import com.intellij.psi.search.ProjectScopeBuilder
import com.intellij.util.indexing.*
import com.intellij.util.io.BooleanDataDescriptor
import java.util.*

/**
 * Invoked during IntelliJ's indexing phase. The DataIndexer marks which files contain a Motif Scope. Once indexed,
 * other parts of the plugin can retrieve all Scope files by invoking ScopeIndex.getScopeFileSuperset().
 */
class ScopeIndex : ScalarIndexExtension<Boolean>(), PsiDependentIndex {

    override fun getIndexer() = DataIndexer<Boolean, Void?, FileContent> { fileContent ->
        val psiJavaFile = fileContent.psiFile as PsiJavaFile
        val isScopeFile = psiJavaFile.classes.find{ it.isScope() } != null
        if (isScopeFile) println("$psiJavaFile")
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

    /**
     * Returns true for all Motif Scope files, but also returns a false positive if the @Scope annotation is not a
     * Motif Scope. This is due to the fact that we can't result annotations' qualified names before indexing has
     * finished.
     */
    private fun PsiClass.isScope(): Boolean {
        return annotations.mapNotNull { it?.nameReferenceElement?.referenceName }
                .find { it == "Scope" || it == "com.uber.motif.Scope" } != null
    }

    companion object {

        private val ID: ID<Boolean, Void> = com.intellij.util.indexing.ID.create("ScopeIndex")

        /**
         * Returns a superset of all files that contain a Motif Scope. See ScopeIndex.isScope() for details.
         */
        fun getScopeFileSuperset(project: Project): Set<VirtualFile> {
            val scopeFiles = mutableSetOf<VirtualFile>()
            val projectScopeBuilder = ProjectScopeBuilder.getInstance(project)
            FileBasedIndex.getInstance().getFilesWithKey(ID, setOf(true), {
                scopeFiles.add(it)
                true
            }, projectScopeBuilder.buildProjectScope())
            return scopeFiles
        }
    }
}