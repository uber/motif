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
package motif.intellij.validation.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.CollectionListModel
import com.intellij.ui.HighlightableComponent
import com.intellij.ui.components.JBList
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.usageView.UsageTreeColorsScheme
import com.intellij.util.ui.UIUtil
import java.awt.Component
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class GraphErrorPanel(private val project: Project) : SimpleToolWindowPanel(true) {

    val content: Content = ServiceManager.getService(ContentFactory::class.java)
            .createContent(this, "Graph Validation", false).apply {
                putUserData(ToolWindow.SHOW_CONTENT_ICON, java.lang.Boolean.TRUE)
            }

    private val documentManager: PsiDocumentManager = PsiDocumentManager.getInstance(project)
    private val model: CollectionListModel<GraphError> = CollectionListModel()
    private val list = JBList<GraphError>(model)

    init {
        setContent(list)
        list.cellRenderer = CellRenderer()
        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    onNavigate()
                    e.consume()
                }
            }
        })

        val actionName = "onNavigate"
        list.getInputMap(JList.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"), actionName)
        list.actionMap.put(actionName, object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                onNavigate()
            }
        })
        GraphStateManager.addListener(project) { state ->
            setState(state)
        }
    }

    private fun setState(state: GraphState) {
        val icon: Icon = if (state.errors.isEmpty()) AllIcons.Process.State.GreenOK else AllIcons.Process.State.RedExcl
        content.icon = icon
        model.removeAll()
        model.addAll(0, state.errors)
    }

    private fun onNavigate() {
        list.selectedValue?.let { selected ->
            val navigatable: Navigatable = selected.psiElement as? Navigatable ?: return
            navigatable.navigate(true)
        }
    }

    private inner class CellRenderer : HighlightableComponent(), ListCellRenderer<GraphError> {

        // Inspired by TodoItemNode
        override fun getListCellRendererComponent(list: JList<out GraphError>, error: GraphError, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            myIsSelected = isSelected
            myHasFocus = cellHasFocus
            font = UIUtil.getTreeFont()
            setText(error.message) // Fallback text in case GraphError doesn't have an associated psiElement.

            val element: PsiElement = error.psiElement ?: return this
            val file: PsiFile = element.containingFile
            val document: Document = documentManager.getDocument(file)!!
            val chars: CharSequence = document.charsSequence
            val lineNumber: Int = document.getLineNumber(element.textRange.startOffset)
            val lineEndOffset = document.getLineEndOffset(lineNumber)
            val lineStartOffset: Int = document.getLineStartOffset(lineNumber).let { lineStart ->
                var fixedLineStart: Int = lineStart
                while (fixedLineStart < document.textLength && chars[fixedLineStart].isWhitespace()) {
                    fixedLineStart++
                }
                fixedLineStart
            }
            val highlightedText: String = chars.subSequence(lineStartOffset, Math.min(lineEndOffset, chars.length)).toString()
            val errorMessage = "${error.message}: "
            setText("$errorMessage$highlightedText")

            val highlighter = HighlighterFactory.createHighlighter(UsageTreeColorsScheme.getInstance().scheme, file.name, project)
            highlighter.setText(chars)
            val iterator = highlighter.createIterator(lineStartOffset)

            while (!iterator.atEnd()) {
                val start = Math.max(iterator.start, lineStartOffset)
                val end = Math.min(iterator.end, lineEndOffset)
                if (lineEndOffset < start || lineEndOffset < end) {
                    break
                }

                addHighlighter(
                        errorMessage.length + start - lineStartOffset,
                        errorMessage.length + end - lineStartOffset,
                        iterator.textAttributes)
                iterator.advance()
            }

            addHighlighter(0, errorMessage.length, TextAttributes(null, null, null, null, Font.BOLD))

            return this
        }
    }
}