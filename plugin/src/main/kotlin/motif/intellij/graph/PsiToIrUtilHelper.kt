package motif.intellij.graph

import com.intellij.psi.*
import motif.intellij.graph.errors.MotifPsiParsingError
import motif.intellij.psi.getClass
import motif.intellij.psi.isScopeClass
import motif.ir.source.accessmethod.AccessMethod
import motif.ir.source.base.Annotation
import motif.ir.source.base.Dependency
import motif.ir.source.base.Type
import motif.ir.source.child.ChildMethod
import motif.ir.source.dependencies.ExplicitDependencies
import motif.ir.source.dependencies.RequiredDependencies
import motif.ir.source.dependencies.RequiredDependency
import motif.ir.source.objects.FactoryMethod
import motif.ir.source.objects.ObjectsClass
import motif.ir.source.objects.SpreadDependency
import motif.ir.source.objects.SpreadMethod

class PsiToIrUtilHelper {

    private val BASE_JAVA_CLASS = "java.lang.Object"

    /**
     * Return a list of ChildMethod
     */
    fun childMethods(psiClass: PsiClass): List<ChildMethod> {
        if (!psiClass.isScopeClass()) return emptyList()
        return getAllAllowedMethods(psiClass)
                .filter { it.returnType.getClass()?.isScopeClass() == true }
                .map { it ->
                    ChildMethod(
                            it,
                            Type(it, psiTypeToStringName(it.returnType)),
                            if (!it.method.hasParameters()) emptyList()
                            else it.method.parameterList.parameters.map { psiParameterToIrDependency(it) }
                    )
                }
    }

    /**
     * Returns a list of AccessMethod
     */
    fun accessMethods(psiClass: PsiClass): List<AccessMethod> {
        if (!psiClass.isScopeClass()) return emptyList()
        return getAllAllowedMethods(psiClass)
                .filter { it.returnType.getClass()?.isScopeClass() == false }
                .map { it -> AccessMethod(it, psiMethodToIrDependency(it.method)) }
    }

    /**
     * Returns the ObjectsClass for the Scope
     */
    fun objectsClass(psiClass: PsiClass): ObjectsClass? {
        assert(psiClass.isScopeClass())
        return psiClass.innerClasses
                .filter { it.hasAnnotation("motif.Objects")}
                .map { it -> ObjectsClass(it, getAllowedMethods(psiClass).map { psiMethodToFactoryMethod(it) }) }
                .firstOrNull()
    }

    /**
     * Returns the Dependencies class for the Scope
     */
    fun explicitDependencies(psiClass: PsiClass): ExplicitDependencies? {
        assert(psiClass.isScopeClass())
        return psiClass.innerClasses
                .filter { it.hasAnnotation("motif.Dependencies") }
                .map { it ->
                    if (it.methods.isEmpty()) ExplicitDependencies(psiClass, emptyList())
                    else ExplicitDependencies(psiClass, getAllowedMethods(it)
                            .map { psiMethodToIrDependency(it) })
                }
                .firstOrNull()
    }

    private fun psiMethodToIrType(psiMethod: PsiMethod): Type {
        return Type(psiMethod, psiTypeToStringName(psiMethod.returnType!!))
    }

    private fun generateAnnotationId(annotation: PsiAnnotation): String {
        return annotation.qualifiedName + annotation.parameterList.attributes.map { "_" + it.value}.sorted()
                .joinToString { it }
    }

    private fun psiParameterToIrType(psiParameter: PsiParameter): Type {
        return Type(psiParameter, psiTypeToStringName(psiParameter.type))
    }

    private fun psiTypeToStringName(psiType: PsiType): String {
        when (psiType) {
            is PsiClassType -> return psiType.getClass()!!.qualifiedName.toString()
            is PsiPrimitiveType -> return psiType.name
        }
        throw RuntimeException("This should not be possible..")
    }

    private fun getQualifierAnnotationIfAny(psiAnnotations: Array<PsiAnnotation>): Annotation? {
        return if(psiAnnotations.isEmpty()) null else {
            // only looks at current annotations, doesn't recurse up the annotations' annotations.
                    psiAnnotations
                            .find {
                                val temp = it.nameReferenceElement?.resolve()
                                temp is PsiClass && temp.hasAnnotation("javax.inject.Qualifier")
                            }
                            .let {
                                return if (it == null) null
                                else Annotation(it.nameReferenceElement?.resolve(), generateAnnotationId(it))
                            }
        }
    }

    private fun psiMethodToFactoryMethod(psiMethod: PsiMethod): FactoryMethod {
        return FactoryMethod(
                psiMethod,
                getPsiMethodKind(psiMethod),
                getPsiMethodScopeClass(psiMethod),
                psiMethod.hasAnnotation("motif.Expose"),
                psiMethod.hasAnnotation("motif.DoNotCache"),
                getPsiMethodRequiredDependencies(psiMethod, getPsiMethodScopeClass(psiMethod)),
                psiMethodToIrDependency(psiMethod),
                psiMethodToSpreadDependency(psiMethod))
    }

    private fun getPsiMethodKind(psiMethod: PsiMethod): FactoryMethod.Kind {
        if (!psiMethod.modifierList.hasModifierProperty(PsiModifier.ABSTRACT)) return FactoryMethod.Kind.BASIC
        return if (psiMethod.hasParameters()) FactoryMethod.Kind.BINDS else FactoryMethod.Kind.CONSTRUCTOR
    }

    private fun getPsiMethodScopeClass(psiMethod: PsiMethod): Type {
        var psiClass = psiMethod.containingClass
        while (psiClass?.isScopeClass() != true && psiClass?.qualifiedName != BASE_JAVA_CLASS) {
            psiClass = psiClass?.containingClass
        }
        if (!psiClass.isScopeClass()) {
            throw MotifPsiParsingError(psiClass, "This should not be possible..")
        }
        return Type(psiClass, psiClass.qualifiedName.toString())
    }

    private fun getPsiMethodRequiredDependencies(psiMethod: PsiMethod, scope: Type): RequiredDependencies {
        if(!psiMethod.hasParameters()) return RequiredDependencies(emptyList())
        return RequiredDependencies(
                psiMethod.parameterList.parameters
                        .map { it -> RequiredDependency(psiParameterToIrDependency(it), false, setOf(scope)) }
        )
    }

    private fun psiMethodToIrDependency(psiMethod: PsiMethod): Dependency {
        return Dependency(psiMethod, psiMethodToIrType(psiMethod), getQualifierAnnotationIfAny(psiMethod.annotations))
    }

    private fun psiParameterToIrDependency(psiParameter: PsiParameter): Dependency {
        return Dependency(psiParameter, psiParameterToIrType(psiParameter), getQualifierAnnotationIfAny(psiParameter.annotations))
    }

    private fun psiMethodToSpreadDependency(psiMethod: PsiMethod): SpreadDependency {
        if (!psiMethod.hasAnnotation("motif.Spread")) return SpreadDependency(emptyList())
        if (psiMethod.returnType !is PsiClassType) throw MotifPsiParsingError(psiMethod, "Cannot annotate primitive with @Spread")
        return SpreadDependency(
                getAllAllowedMethods(psiMethod.returnType?.getClass()!!)
                        .filter { !it.method.hasParameters() }
                        .map {
                            // what if the spread method should have no RequriedDependencies..
                            SpreadMethod(
                                    it.method,
                                    RequiredDependency(Dependency(null, Type(null, it.method.name), null), false, setOf()),
                                    Dependency(
                                            it.method,
                                            Type(it.method, psiTypeToStringName(it.returnType)),
                                            getQualifierAnnotationIfAny(it.method.annotations)))
                        }
        )
    }

    private fun getAllowedMethods(psiClass: PsiClass): Array<PsiMethod> {
        psiClass.methods.forEach { if (it.isConstructor) throw MotifPsiParsingError(it, "Constructors not allowed here") }
        return psiClass.methods
    }

    private fun getAllAllowedMethods(psiClass: PsiClass): Array<IntermediateMethodRepresentation> {
        return psiClass.allMethodsAndTheirSubstitutors
                .filter { it.first.containingClass?.qualifiedName != BASE_JAVA_CLASS }
                .map {it ->
                    if (it.first.isConstructor) throw MotifPsiParsingError(it.first, "Constructors not allowed here")
                    else it
                }
                .map { if (it.second !is EmptySubstitutor) {
                    IntermediateMethodRepresentation(it.first, it.second.substitutionMap.values.first())
                } else IntermediateMethodRepresentation(it.first, it.first.returnType!!) }
                .toTypedArray()
    }

    data class IntermediateMethodRepresentation(val method: PsiMethod, val returnType: PsiType)


//            return if (psiAnnotations.isEmpty()) null else {
//            var qualifier: PsiAnnotation? = null
//            val stack: Stack<PsiAnnotation> = Stack()
//            psiAnnotations.forEach { stack.push(it) }
//            while(!stack.empty() && qualifier == null) {
//                val temp = stack.pop().nameReferenceElement?.resolve()
    // changed some stuff here, will need fixing
//                if (temp is PsiClass && temp.hasAnnotation("javax.inject.Qualifier") && temp is PsiAnnotation) {
//                        qualifier = temp
//                        break
//                 } else {
//                        temp.annotations.forEach { stack.push(it) }
//                }
//            }
//            return if (qualifier == null) null else Annotation(qualifier, generateAnnotationId(qualifier))
//        }
}