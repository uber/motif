package com.uber.motif.intellij.index

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.impl.source.JavaFileElementType
import com.intellij.util.indexing.*
import com.intellij.util.io.BooleanDataDescriptor
import com.uber.motif.intellij.psi.isMaybeScopeFile
import java.util.*

/**
 * Invoked during IntelliJ's indexing phase. The DataIndexer marks which files contain a Motif Scope. Once indexed,
 * other parts of the plugin can retrieve all Scope files via ProjectScopeIndex.
 */
class ScopeIndex : ScalarIndexExtension<Boolean>(), PsiDependentIndex {

    override fun getIndexer() = DataIndexer<Boolean, Void?, FileContent> { fileContent ->
        val psiFile = fileContent.psiFile
        val isScopeFile = psiFile.isMaybeScopeFile()
        if (isScopeFile) println("INDEX UPDATE: $psiFile${Thread.currentThread()}")
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

    companion object {

        val ID: ID<Boolean, Void> = com.intellij.util.indexing.ID.create("ScopeIndex")
    }
}