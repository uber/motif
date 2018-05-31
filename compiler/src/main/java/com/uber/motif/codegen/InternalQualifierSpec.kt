package com.uber.motif.compiler.codegen

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import com.uber.motif.compiler.graph.ResolvedScope
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
            .build()
}