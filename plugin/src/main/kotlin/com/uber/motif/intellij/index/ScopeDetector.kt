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

/**
 * Detects whether a FileContent object contains a class with a Scope annotation. Note that we can only do rudimentary
 * string comparisons at the indexing stage, so maybeHasScope will return true for all Motif Scope files, but will
 * return a false positive if the @Scope annotation is not a Motif Scope.
 */
class ScopeDetector private constructor(private val tree: LighterAST) {

    fun maybeHasScope(): Boolean {
        val root: LighterASTNode = tree.root
        val annotationId: LighterASTTokenNode = root.firstChild(JavaElementType.CLASS)
                ?.firstChild(JavaElementType.MODIFIER_LIST)
                ?.firstChild(JavaElementType.ANNOTATION)
                ?.firstChild(JavaElementType.JAVA_CODE_REFERENCE)
                ?.firstChild(JavaTokenType.IDENTIFIER) as LighterASTTokenNode? ?: return false
        val name = tree.charTable.intern(annotationId.text).toString()
        return name == "Scope" || name == "com.uber.motif.Scope"
    }

    private fun LighterASTNode.firstChild(type: IElementType): LighterASTNode? {
        return LightTreeUtil.firstChildOfType(tree, this, type)
    }

    companion object {

        fun maybeHasScope(fileContent: FileContent): Boolean {
            val tree: LighterAST = (fileContent as FileContentImpl).lighterASTForPsiDependentIndex
            return ScopeDetector(tree).maybeHasScope()
        }
    }
}