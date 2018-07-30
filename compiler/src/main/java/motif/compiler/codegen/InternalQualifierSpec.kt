package motif.compiler.codegen

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import motif.compiler.graph.ResolvedScope
import motif.compiler.model.Dependency
import motif.compiler.serialize
import java.lang.annotation.RetentionPolicy
import javax.inject.Qualifier
import javax.lang.model.element.Modifier

class InternalQualifierSpec(resolvedScope: ResolvedScope) {

    val className: ClassName = resolvedScope.scopeImplName.nestedClass("Internal")

    val spec: TypeSpec = TypeSpec.annotationBuilder(className)
            .addAnnotation(AnnotationSpec.builder(ClassName.get("java.lang.annotation", "Retention"))
                    .addMember("value", "\$T.CLASS", RetentionPolicy::class.java)
                    .build())
            .addModifiers(Modifier.PRIVATE)
            .addAnnotation(Qualifier::class.java)
            .addMethod(MethodSpec.methodBuilder("value")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(String::class.java)
                    .defaultValue("\"\"")
                    .build())
            .build()

    fun annotationSpec(dependency: Dependency, isInternal: Boolean): AnnotationSpec? {
        return if (isInternal) {
            AnnotationSpec.builder(className).apply {
                dependency.qualifier?.let {
                    // Since Dagger doesn't allow multiple qualifiers, merge internal qualifier and user defined
                    // qualifier by serializing the user defined annotation into @Internal's value,
                    addMember("value", "\"${it.serialize()}\"")
                }
            }.build()
        } else {
            dependency.qualifier?.let { AnnotationSpec.get(it) }
        }
    }
}