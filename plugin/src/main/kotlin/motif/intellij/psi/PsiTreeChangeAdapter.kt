/*
 * Copyright (c) 2018 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif.intellij.psi

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