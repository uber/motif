package motif.intellij.graph.errors

import com.intellij.psi.PsiElement

class MotifPsiParsingError(val element: PsiElement?, val errorMessage: String) : RuntimeException(errorMessage)