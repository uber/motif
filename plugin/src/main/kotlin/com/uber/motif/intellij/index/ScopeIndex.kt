package com.uber.motif.intellij.index

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaTokenType
import com.intellij.psi.impl.source.JavaFileElementType
import com.intellij.psi.impl.source.tree.JavaElementType
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.search.ProjectScopeBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.util.indexing.*
import com.intellij.util.io.BooleanDataDescriptor
import java.util.*

class ScopeIndex : ScalarIndexExtension<Boolean>() {

    override fun getIndexer() = DataIndexer<Boolean, Void?, FileContent> { fileContent ->
        val tree: LighterAST = (fileContent as FileContentImpl).lighterASTForPsiDependentIndex
        if (ScopeDetector(tree).hasScope()) {
            mapOf(true to null)
        } else {
            mapOf()
        }
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

    private class ScopeDetector(private val tree: LighterAST) {

        fun hasScope(): Boolean {
            val root: LighterASTNode = tree.root
            val annotationId: LighterASTTokenNode = root.firstChild(JavaElementType.CLASS)
                    ?.firstChild(JavaElementType.MODIFIER_LIST)
                    ?.firstChild(JavaElementType.ANNOTATION)
                    ?.firstChild(JavaElementType.JAVA_CODE_REFERENCE)
                    ?.firstChild(JavaTokenType.IDENTIFIER) as LighterASTTokenNode? ?: return false
            val name = tree.charTable.intern(annotationId.text).toString()
            return name == "Scope"
        }

        private fun LighterASTNode.firstChild(type: IElementType): LighterASTNode? {
            return LightTreeUtil.firstChildOfType(tree, this, type)
        }
    }

    companion object {

        private val ID: ID<Boolean, Void> = com.intellij.util.indexing.ID.create("ScopeIndex")

        fun getScopeFiles(project: Project): Set<VirtualFile> {
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