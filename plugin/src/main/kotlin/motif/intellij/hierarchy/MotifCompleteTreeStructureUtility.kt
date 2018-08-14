package motif.intellij.hierarchy

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.type.TypeHierarchyNodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import motif.intellij.MotifComponent

class MotifCompleteTreeStructureUtility {

    /**
     * Creates the tree top down, starting from the top most parent, down to the scope at hand. This allows us to
     * pass the scope node to HierarchyTreeStructure class to build its children aka the lower tree using the Subtree
     * logic.
     */
    fun buildParentHierarchy(psiClass: PsiClass, project: Project): HierarchyNodeDescriptor {
        val component = MotifComponent.get(project)
        val scopeToParentsMap: Map<PsiClass, List<PsiClass>> = component.graphProcessor.scopeToParentsMap()
        var descriptor: HierarchyNodeDescriptor? = null
        val parents: Array<PsiClass> = getTopDownParentHierarchy(psiClass, scopeToParentsMap)
        parents.forEach {
            val newDescriptor: HierarchyNodeDescriptor = TypeHierarchyNodeDescriptor(project, descriptor,
                    it, false)
            if (descriptor != null) {
                descriptor?.cachedChildren = arrayOf(newDescriptor)
            }
            descriptor = newDescriptor
        }
        val newDescriptor: HierarchyNodeDescriptor = TypeHierarchyNodeDescriptor(project, descriptor, psiClass, true)
        descriptor?.cachedChildren = arrayOf(newDescriptor)
        return newDescriptor
    }

    /**
     * Recursively iterates upwards through the parent hierarchy, adding them to an array in order. Returns an array
     * of parents, starting form the top, all the way down to the scope's immediate parent.
     */
    private fun getTopDownParentHierarchy(psiClass: PsiClass, scopeToParentsMap: Map<PsiClass, List<PsiClass>>): Array<PsiClass> {
        val parentScopes: MutableList<PsiClass> = mutableListOf()
        var parentList: List<PsiClass>? = scopeToParentsMap[psiClass] ?: return parentScopes.toTypedArray()
        var index = 0
        while (parentList != null && !parentList.isEmpty()) {
            // TODO(currently handling only 1 parent hierarchy. Need to add support for handling multiple parents.)
            val parent: PsiClass = parentList[0]
            parentScopes.add(index++, parent)
            parentList = scopeToParentsMap[parent]
        }
        return parentScopes.toTypedArray().reversedArray()
    }
}
