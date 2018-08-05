package motif.compiler

import motif.Scope
import motif.compiler.codegen.Generator
import motif.compiler.errors.CompilationError
import motif.compiler.errors.MissingDependenciesError
import motif.compiler.ir.ScopeClassFactory
import motif.ir.graph.GraphFactory
import motif.ir.source.SourceSet
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

class Processor(private val listener: Listener? = null) : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Scope::class.java.name)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            process(roundEnv)
        } catch (e: CompilationError) {
            listener?.onError(e)
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.message)
        }
        return true
    }

    fun process(roundEnv: RoundEnvironment) {
        val factory = ScopeClassFactory(processingEnv)
        val scopes = roundEnv.getElementsAnnotatedWith(Scope::class.java)
                .map { it as TypeElement }
                .map { it.asType() as DeclaredType }
                .map { factory.create(it) }

        val graph = GraphFactory.create(SourceSet(scopes))
        graph.missingDependencies?.let { throw MissingDependenciesError(it) }

        Generator(processingEnv, graph).generate()
    }

    interface Listener {

        fun onError(error: CompilationError)
    }
}