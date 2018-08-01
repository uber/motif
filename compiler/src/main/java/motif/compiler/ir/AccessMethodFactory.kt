package motif.compiler.ir

import motif.compiler.javax.Executable
import motif.compiler.javax.JavaxUtil
import motif.ir.source.accessmethod.AccessMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.TypeKind

class AccessMethodFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    fun isApplicable(executable: Executable): Boolean {
        return executable.parameters.isEmpty() && executable.returnType.kind != TypeKind.VOID
    }

    fun create(executable: Executable): AccessMethod {
        return AccessMethod(executable, executable.returnedDependency)
    }
}