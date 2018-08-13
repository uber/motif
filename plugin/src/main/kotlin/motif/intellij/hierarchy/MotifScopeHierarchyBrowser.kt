package motif.intellij.hierarchy

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.ide.hierarchy.JavaHierarchyUtil
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase
import com.intellij.ide.hierarchy.type.TypeHierarchyNodeDescriptor
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import motif.intellij.psi.isScopeClass
import java.util.Comparator
import javax.swing.JPanel
import javax.swing.JTree

class MotifScopeHierarchyBrowser(project: Project?, element: PsiElement?) : TypeHierarchyBrowserBase(project, element) {

    override fun getQualifiedName(psiElement: PsiElement?): String {
        return if (psiElement is PsiClass) psiElement.qualifiedName!! else ""
    }

    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is PsiClass && element.isScopeClass()
    }

    override fun isInterface(psiElement: PsiElement): Boolean {
        return false
    }

    override fun getComparator(): Comparator<NodeDescriptor<Any>>? {
        return JavaHierarchyUtil.getComparator(myProject)
    }

    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        return (descriptor as? TypeHierarchyNodeDescriptor)?.psiClass
    }

    override fun createLegendPanel(): JPanel? {
        return null
    }

    override fun createTrees(trees: MutableMap<String, JTree>) {
        createTreeAndSetupCommonActions(trees, IdeActions.GROUP_TYPE_HIERARCHY_POPUP)
    }

    override fun createHierarchyTreeStructure(typeName: String, psiElement: PsiElement): HierarchyTreeStructure? {
        if (typeName == TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE) {
               val completeTreeStructure = MotifCompleteTreeStructureUtility()
               val descriptor: HierarchyNodeDescriptor = completeTreeStructure.buildParentHierarchy(psiElement as
                PsiClass, myProject)
                return MotifScopeHierarchyTreeStructure(myProject, descriptor)
            }
        return MotifScopeHierarchyTreeStructure(myProject, psiElement as PsiClass, typeName)
    }

    override fun canBeDeleted(psiElement: PsiElement?): Boolean {
        return false
    }
}