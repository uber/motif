package com.uber.motif.intellij.psi

import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener

abstract class PsiTreeChangeAdapter : PsiTreeChangeListener {

    abstract fun onChange(event: PsiTreeChangeEvent)

    override fun propertyChanged(event: PsiTreeChangeEvent) {
        onChange(event)
    }

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        onChange(event)
    }

    override fun childAdded(event: PsiTreeChangeEvent) {
        onChange(event)
    }

    override fun childRemoved(event: PsiTreeChangeEvent) {
        onChange(event)
    }

    override fun childReplaced(event: PsiTreeChangeEvent) {
        onChange(event)
    }

    override fun childMoved(event: PsiTreeChangeEvent) {
        onChange(event)
    }

    override fun beforePropertyChange(event: PsiTreeChangeEvent) {}

    override fun beforeChildrenChange(event: PsiTreeChangeEvent) {}

    override fun beforeChildAddition(event: PsiTreeChangeEvent) {}

    override fun beforeChildRemoval(event: PsiTreeChangeEvent) {}

    override fun beforeChildReplacement(event: PsiTreeChangeEvent) {}

    override fun beforeChildMovement(event: PsiTreeChangeEvent) {}
}