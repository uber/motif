package motif.compiler.codegen

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import dagger.Component
import motif.cache.ExtCacheScope
import motif.internal.DaggerScope
import motif.ir.graph.Graph
import motif.ir.graph.Scope
import motif.ir.source.accessmethod.AccessMethod
import motif.ir.source.objects.ObjectsClass
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier

class ScopeImplFactory(
        env: ProcessingEnvironment,
        cacheScope: ExtCacheScope,
        graph: Graph) : CodegenCache(env, cacheScope) {

    private val childMethodFactory = ChildMethodFactory(env, cacheScope, graph)
    private val moduleFactory = ModuleFactory(env, cacheScope)

    fun create(scope: Scope): TypeSpec {
        val childMethods = scope.childDeclarations.map { childMethodFactory.create(scope, it) }
        val accessMethodImpls = scope.accessMethods.map { it.implSpec(scope) }

        return TypeSpec.classBuilder(scope.implTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(scope.typeName)
                .addField(scope.componentFieldSpec)
                .addType(dependencies(scope))
                .addType(component(scope))
                .addObjectsImplSpec(scope)
                .addType(moduleFactory.create(scope))
                .addMethod(scope.constructorSpec())
                .addAltConstructorSpec(scope)
                .addMethods(childMethods)
                .addMethods(accessMethodImpls)
                .build()
                .write(scope.packageName)
    }

    private fun component(scope: Scope): TypeSpec {
        return TypeSpec.interfaceBuilder(scope.componentTypeName)
                .addAnnotation(DaggerScope::class.java)
                .addAnnotation(AnnotationSpec.builder(Component::class.java)
                        .addMember("dependencies", "\$T.class", scope.dependenciesTypeName)
                        .addMember("modules", "\$T.class", scope.moduleTypeName)
                        .build())
                .addMethods(scope.componentMethodSpecs.map { it.value })
                .build()
    }

    private fun dependencies(scope: Scope): TypeSpec {
        return TypeSpec.interfaceBuilder(scope.dependenciesTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addMethods(scope.dependencies.abstractMethodSpecsMeta().map { it.value })
                .build()
    }

    private fun TypeSpec.Builder.addObjectsImplSpec(scope: Scope): TypeSpec.Builder {
        scope.objectsClass?.let {
            addType(it.implSpec(scope))
        }
        return this
    }

    private fun TypeSpec.Builder.addAltConstructorSpec(scope: Scope): TypeSpec.Builder {
        if (!scope.dependencies.list.isEmpty()) return this

        val spec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this(new \$T() {})", scope.dependenciesTypeName)
                .build()
        addMethod(spec)
        return this
    }

    private fun Scope.constructorSpec(): MethodSpec {
        val dependenciesParam: ParameterSpec = ParameterSpec.builder(dependenciesTypeName, "dependencies").build()
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(dependenciesParam)
                .addStatement("this.\$N = \$T.builder()\n" +
                        ".\$N(\$N)\n" +
                        ".\$N(new \$T())\n" +
                        ".build()",
                        componentFieldSpec,
                        daggerComponentName,
                        dependenciesTypeName.simpleName().decapitalize(),
                        dependenciesParam,
                        moduleTypeName.simpleName().decapitalize(),
                        moduleTypeName)
                .build()
    }

    private fun ObjectsClass.implSpec(scope: Scope): TypeSpec {
        val overriddenMethods = abstractFactoryMethods().map { it.overrideUnsupported() }
        return TypeSpec.classBuilder(scope.objectsImplTypeName)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .apply { if (isInterface) addSuperinterface(typeName) else superclass(typeName) }
                .addMethods(overriddenMethods)
                .build()
    }

    private fun AccessMethod.implSpec(scope: Scope): MethodSpec {
        val componentMethod = scope.componentMethodSpecs[dependency]
                ?: throw IllegalStateException("Could not find component method for AccessMethod: $dependency")
        return executable.overriding()
                .addStatement("return \$N.\$N()", scope.componentFieldSpec, componentMethod)
                .build()
    }
}