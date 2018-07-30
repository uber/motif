package motif.compiler.codegen

import com.squareup.javapoet.FieldSpec
import javax.lang.model.element.Modifier

class ComponentFieldSpec(componentSpec: ComponentSpec) {

    val spec: FieldSpec = FieldSpec.builder(componentSpec.className, "component", Modifier.PRIVATE, Modifier.FINAL)
            .build()
}