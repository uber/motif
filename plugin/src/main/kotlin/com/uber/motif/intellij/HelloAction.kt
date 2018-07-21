package com.uber.motif.intellij

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.uber.motif.intellij.index.ScopeIndex

class HelloAction : AnAction("Hello") {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project!!
        FileBasedIndex.getInstance().getFilesWithKey(ScopeIndex.ID, setOf("LELAND"), { file ->
            println(file)
            true
        }, GlobalSearchScope.allScope(project))
    }
}