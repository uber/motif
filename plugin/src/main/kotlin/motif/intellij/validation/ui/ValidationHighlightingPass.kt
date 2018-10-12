package motif.intellij.validation.ui

import com.intellij.codeHighlighting.TextEditorHighlightingPass
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElementFactory
import motif.intellij.validation.index.ScopeIndex
import motif.intellij.validation.ir.IntelliJType
import motif.models.graph.Graph
import motif.models.graph.GraphFactory
import motif.models.java.IrType
import motif.models.motif.SourceSet
import motif.models.parsing.SourceSetParser
import motif.models.parsing.errors.ParsingError

class ValidationHighlightingPass private constructor(private val project: Project, document: Document) : TextEditorHighlightingPass(project, document) {

    private var result: Result? = null

    constructor(editor: Editor) : this(editor.project!!, editor.document)

    override fun doCollectInformation(progress: ProgressIndicator) {
        result = validateGraph()
    }

    override fun doApplyInformationToEditor() {
        val result: Result = this.result ?: throw IllegalStateException()

        result.parsingError?.let {
            println(it)
        }

        result.graph?.validationErrors?.let { println(it) }
    }

    private fun validateGraph(): Result {
        val sourceSet: SourceSet = try {
            parseSourceSet()
        } catch (e: ParsingError) {
            return Result(null, e)
        }

        val graph: Graph = GraphFactory.create(sourceSet)
        return Result(graph, null)
    }

    private fun parseSourceSet(): SourceSet {
        val scopeClasses: List<PsiClass> = ScopeIndex.getInstance().getScopeClasses(project)
        val scopeAnnotatedTypes: Set<IrType> = scopeClasses
                // TODO remove these filters
                .filter { !it.containingFile.containingDirectory.toString().contains("test") && !it.containingFile.containingDirectory.toString().contains("external")}
                .map { psiClass ->
                    val psiClassType: PsiClassType = PsiElementFactory.SERVICE.getInstance(project).createType(psiClass)
                    IntelliJType(project, psiClassType)
                }
                .toSet()

        return SourceSetParser().parse(scopeAnnotatedTypes)
    }

    private data class Result(val graph: Graph?, val parsingError: ParsingError?)
}