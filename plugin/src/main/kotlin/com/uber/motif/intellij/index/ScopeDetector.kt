package com.uber.motif.intellij.index

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.psi.JavaTokenType
import com.intellij.psi.impl.source.tree.JavaElementType
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.FileContentImpl

class ScopeDetector private constructor(private val tree: LighterAST) {

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

    companion object {

        fun hasScope(fileContent: FileContent): Boolean {
            val tree: LighterAST = (fileContent as FileContentImpl).lighterASTForPsiDependentIndex
            return ScopeDetector(tree).hasScope()
        }
    }
}