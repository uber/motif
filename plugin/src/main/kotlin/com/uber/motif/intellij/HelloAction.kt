package com.uber.motif.intellij

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.uber.motif.intellij.index.ScopeIndex

class HelloAction : AnAction("Hello") {

    override fun actionPerformed(event: AnActionEvent) {
        println(ScopeIndex.getScopeFiles(event.project!!))
    }
}