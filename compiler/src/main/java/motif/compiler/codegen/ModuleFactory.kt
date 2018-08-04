package motif.compiler.codegen

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import dagger.Module
import dagger.Provides
import motif.internal.DaggerScope
import motif.ir.graph.Scope
import motif.ir.source.base.Dependency
import motif.ir.source.objects.FactoryMethod
import motif.ir.source.objects.ObjectsClass
import motif.ir.source.objects.SpreadMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier

class ModuleFactory(
        env: ProcessingEnvironment,
        cacheScope: CacheScope) : CodegenCache(env, cacheScope) {

    fun create(scope: Scope): TypeSpec {
        val builder = TypeSpec.classBuilder(scope.moduleTypeName)
                .addAnnotation(Module::class.java)
        val objectsClass = scope.objectsClass ?: return builder.build()
        val objectsField = FieldSpec.builder(objectsClass.typeName, "objects", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new \$T()", scope.objectsImplTypeName)
                .build()
        builder.addField(objectsField)
        builder.build(scope, objectsClass, objectsField)
        return builder.build()
    }

    private fun TypeSpec.Builder.build(
            scope: Scope,
            objectsClass: ObjectsClass,
            objectsField: FieldSpec) {
        nameScope {
            fun Dependency.providerMethod(isCached: Boolean): MethodSpec.Builder {
                return methodSpecBuilder()
                        .addAnnotation(Provides::class.java)
                        .apply { if (isCached) addAnnotation(DaggerScope::class.java) }
            }

            val scopeMethod = scope.scopeDependency.providerMethod(false)
                    .addStatement("return \$T.this", scope.implTypeName)
                    .build()
            addMethod(scopeMethod)

            val methods = objectsClass.factoryMethods
                    .flatMap { factoryMethod ->
                        val spec = factoryMethod.providedDependency
                                .providerMethod(factoryMethod.isCached)
                                .build(objectsField, factoryMethod)
                        val spreadSpecs = factoryMethod.spreadDependency?.methods?.map { spreadMethod ->
                            spreadMethod.dependency
                                    .providerMethod(factoryMethod.isCached)
                                    .build(spreadMethod)
                        } ?: listOf()
                        spreadSpecs + spec
                    }
            addMethods(methods)
        }
    }

    private fun MethodSpec.Builder.build(objectsField: FieldSpec, method: FactoryMethod): MethodSpec {
        val dependency = method.providedDependency
        val parameters = nameScope {
            method.consumedDependencies.map { it.parameterSpec() }
        }.apply { addParameters(this) }.toTypedArray()
        val callParams: String = parameters.joinToString(", ") { "\$N" }

        val returnStatement = when (method.kind) {
            FactoryMethod.Kind.BASIC -> CodeBlock.of("return \$N.\$N($callParams)", objectsField, method.executable.name, *parameters)
            FactoryMethod.Kind.BINDS -> CodeBlock.of("return $callParams", *parameters)
            FactoryMethod.Kind.CONSTRUCTOR -> CodeBlock.of("return new \$T($callParams)", dependency.typeName, *parameters)
        }

        addStatement(returnStatement)

        return build()
    }

    private fun MethodSpec.Builder.build(method: SpreadMethod): MethodSpec {
        val spreadParameter = nameScope { method.source.parameterSpec() }
        addParameter(spreadParameter)
        addStatement("return \$N.\$N()", spreadParameter, method.executable.name)
        return build()
    }
}