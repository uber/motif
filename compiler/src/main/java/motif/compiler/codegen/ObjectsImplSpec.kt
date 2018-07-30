package motif.compiler.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import motif.compiler.asTypeElement
import motif.compiler.graph.ResolvedScope
import motif.compiler.names.Names
import javax.lang.model.element.ElementKind

class ObjectsImplSpec(
        resolvedScope: ResolvedScope) {

    val className: ClassName = resolvedScope.scopeImplName.nestedClass(Names.OBJECTS_CLASS_NAME)
    val spec: TypeSpec? = resolvedScope.scope.objectsClass?.let { objectsClass ->
        TypeSpec.classBuilder(className).apply {
            if (objectsClass.type.asTypeElement().kind == ElementKind.INTERFACE) {
                addSuperinterface(objectsClass.className)
            } else {
                superclass(objectsClass.className)
            }
            objectsClass.abstractProviderMethods.map {
                it.override()
                        .addStatement("throw new \$T()", UnsupportedOperationException::class.java)
                        .build()
            }.forEach { addMethod(it) }
        }.build()
    }
}