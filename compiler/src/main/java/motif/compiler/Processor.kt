package motif.compiler

import motif.Scope
import motif.compiler.codegen.Generator
import motif.compiler.ir.ScopeClassFactory
import motif.ir.graph.GraphErrors
import motif.ir.graph.GraphFactory
import motif.ir.source.SourceSet
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

class Processor : AbstractProcessor() {

    // For testing only.
    var errors: GraphErrors? = null

    private val hasErrors: Boolean
        get() = !(errors?.isEmpty ?: true)

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Scope::class.java.name)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.processingOver() || hasErrors) {
            return true
        }
        process(roundEnv)
        return true
    }

    fun process(roundEnv: RoundEnvironment) {
        val factory = ScopeClassFactory(processingEnv)
        val scopes = roundEnv.getElementsAnnotatedWith(Scope::class.java)
                .map { it as TypeElement }
                .map { it.asType() as DeclaredType }
                .map { factory.create(it) }

        val graph = GraphFactory.create(SourceSet(scopes))
        val errors = graph.graphErrors
        this.errors = errors

        if (hasErrors) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, errors.getMessage())
        } else {
            Generator(processingEnv, graph).generate()
        }
    }
}