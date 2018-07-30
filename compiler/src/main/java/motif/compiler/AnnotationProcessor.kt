package motif.compiler

import com.squareup.javapoet.JavaFile
import motif.Scope
import motif.compiler.codegen.ScopeImplSpec
import motif.compiler.graph.ResolvedGraph
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

class AnnotationProcessor(private val errorListener: ErrorListener?) : AbstractProcessor() {

    constructor() : this(null)

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Scope::class.java.name)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val scopes = roundEnv.getElementsAnnotatedWith(Scope::class.java)
                .map { it as TypeElement }
                .map { it.asType() as DeclaredType }
        val graph = try {
            ResolvedGraph.resolve(processingEnv, scopes)
        } catch (e: CompilationError) {
            errorListener?.onError(e)
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.message)
            return true
        }
        graph.resolvedScopes.forEach {
            val scopeImpl = ScopeImplSpec(it)
            JavaFile.builder(it.packageName, scopeImpl.spec).build().writeTo(processingEnv.filer)
        }
        return true
    }

    interface ErrorListener {

        fun onError(error: CompilationError)
    }
}