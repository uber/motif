package com.uber.motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import javax.lang.model.element.Modifier

class ScopeConstructorSpec(
        component: ComponentSpec,
        componentField: ComponentFieldSpec,
        module: ModuleSpec,
        parentInterface: ParentInterfaceSpec) {

    private val parentParameter: ParameterSpec = ParameterSpec.builder(parentInterface.className, "parent").build()

    val spec: MethodSpec = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(parentParameter)
            .addStatement("this.\$N = \$T.builder()\n" +
                    ".parent(\$N)\n" +
                    ".module(new \$T())\n" +
                    ".build()",
                    componentField.spec,
                    component.daggerClassName,
                    parentParameter,
                    module.className)
            .build()

    val altSpec: MethodSpec? = if (parentInterface.isEmpty) {
        MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.\$N = \$T.builder()\n" +
                        ".parent(new \$T() {})\n" +
                        ".module(new \$T())\n" +
                        ".build()",
                        componentField.spec,
                        component.daggerClassName,
                        parentInterface.className,
                        module.className)
                .build()
    } else {
        null
    }
}