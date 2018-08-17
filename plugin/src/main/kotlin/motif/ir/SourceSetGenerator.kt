package motif.ir

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import motif.intellij.MotifComponent
import motif.intellij.graph.PsiToIrUtilHelper
import motif.ir.source.ScopeClass
import motif.ir.source.SourceSet
import motif.ir.source.base.Type

class SourceSetGenerator {

    fun createSourceSet(project: Project): SourceSet {

        val validationHelper = PsiToIrUtilHelper()
        val allScopes: List<PsiClass> = MotifComponent.get(project).graphProcessor.scopeClasses()

        return SourceSet(
                allScopes
                        // TODO: remove these filters
                        .filter { !it.containingFile.containingDirectory.toString().contains("test") && !it.containingFile.containingDirectory.toString().contains("external")}
//                        .filter { it.qualifiedName.toString() == "motif.sample.app.root.RootScope" }
                        .map { it -> ScopeClass(
                                it,
                                Type(it, it.qualifiedName.toString()),
                                validationHelper.childMethods(it),
                                validationHelper.accessMethods(it),
                                validationHelper.objectsClass(it),
                                validationHelper.explicitDependencies(it))
                        }
        )
    }
}
