package com.uber.motif.intellij

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.uber.motif.intellij.model.Playground

class HelloAction : AnAction("Hello") {

    override fun actionPerformed(event: AnActionEvent) {
        Playground.run(event.project!!)
    }

    override fun beforeActionPerformedUpdate(e: AnActionEvent) {
        println(e)
    }
}