package motif.compiler.ir

import com.google.auto.common.MoreTypes
import motif.Scope
import motif.compiler.ir
import motif.compiler.javax.Executable
import motif.compiler.javax.JavaxUtil
import motif.ir.source.child.ChildMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

class ChildDeclarationFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    fun isApplicable(executable: Executable): Boolean {
        val returnMirror: TypeMirror = executable.returnType
        if (returnMirror.kind != TypeKind.DECLARED) {
            return false
        }

        val returnType: DeclaredType = returnMirror as DeclaredType
        return MoreTypes.asElement(returnType).hasAnnotation(Scope::class)
    }

    fun create(executable: Executable): ChildMethod {
        val childScopeType: DeclaredType = executable.returnType as DeclaredType
        val dynamicDependencies = executable.parameters.map { it.dependency }
        return ChildMethod(executable, childScopeType.ir, dynamicDependencies)
    }
}