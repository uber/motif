/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
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
package motif.intellij

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.PsiWhiteSpace

class ChildChangeListener(private val callback: (PsiElement) -> Unit) : PsiTreeChangeListener {

  override fun beforePropertyChange(event: PsiTreeChangeEvent) {
    log("beforePropertyChange", event)
  }

  override fun beforeChildAddition(event: PsiTreeChangeEvent) {
    log("beforeChildAddition", event)
  }

  override fun beforeChildReplacement(event: PsiTreeChangeEvent) {
    log("beforeChildReplacement", event)
  }

  override fun beforeChildrenChange(event: PsiTreeChangeEvent) {
    log("beforeChildrenChange", event)
  }

  override fun beforeChildMovement(event: PsiTreeChangeEvent) {
    log("beforeChildMovement", event)
  }

  override fun beforeChildRemoval(event: PsiTreeChangeEvent) {
    log("beforeChildRemoval", event)
  }

  override fun childrenChanged(event: PsiTreeChangeEvent) {
    log("childrenChanged", event)
  }

  override fun propertyChanged(event: PsiTreeChangeEvent) {
    log("propertyChanged", event)
  }

  override fun childReplaced(event: PsiTreeChangeEvent) {
    log("childReplaced", event)
    if (event.oldChild is PsiWhiteSpace && event.newChild is PsiWhiteSpace) return
    callback(event.child)
  }

  override fun childMoved(event: PsiTreeChangeEvent) {
    log("childMoved", event)
    if (event.child is PsiWhiteSpace) return
    callback(event.child)
  }

  override fun childRemoved(event: PsiTreeChangeEvent) {
    log("childRemoved", event)
    if (event.child is PsiWhiteSpace) return
    callback(event.child)
  }

  override fun childAdded(event: PsiTreeChangeEvent) {
    log("childAdded", event)
    if (event.child is PsiWhiteSpace) return
    callback(event.child)
  }

  private fun log(name: String, event: PsiTreeChangeEvent) {
    //        log("""=======$name=======
    //            |parent: ${event.parent}
    //            |oldParent: ${event.oldParent}
    //            |newParent: ${event.newParent}
    //            |child: ${event.child}
    //            |oldChild: ${event.oldChild}
    //            |newChild: ${event.newChild}
    //            |element: ${event.element}
    //            |propertyName: ${event.propertyName}
    //            |oldValue: ${event.oldValue}
    //            |newValue: ${event.newValue}
    //        """.trimMargin())
  }
}
