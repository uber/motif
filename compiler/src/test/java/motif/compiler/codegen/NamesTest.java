/*
 * Copyright (c) 2018 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif.compiler.codegen;

import com.google.common.collect.ImmutableSet;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class NamesTest {

    @Test
    public void basic() {
        String name = getName("java.util.HashMap");
        assertThat(name).isEqualTo("hashMap");
    }

    @Test
    public void typeArgument() {
        String name = getName("java.util.HashMap", "String", "Integer");
        assertThat(name).isEqualTo("stringIntegerHashMap");
    }

    @Test
    public void wildcard() {
        String name = getName("java.util.HashMap", "? extends String", "? super Integer");
        assertThat(name).isEqualTo("stringIntegerHashMap");
    }

    @Test
    public void typeVariable() {
        String name = getName("java.util.HashMap", "String", "A");
        assertThat(name).isEqualTo("stringAHashMap");
    }

    @Test
    public void nested() {
        String name = getName("java.util.HashMap", "java.util.HashMap<String, Integer>", "Integer");
        assertThat(name).isEqualTo("stringIntegerHashMapIntegerHashMap");
    }

    private static String getName(String className, String... typeArguments) {
        String typeArgumentString = String.join(",", typeArguments);
        if (!typeArgumentString.isEmpty()) {
            typeArgumentString = "<" + typeArgumentString + ">";
        }
        SafeNameProcessor processor = new SafeNameProcessor();
        Compilation compilation = javac()
                .withProcessors(processor)
                .compile(JavaFileObjects.forSourceLines(
                        "test.Test",
                        "package test;",
                        "class Test<A extends String> {",
                        className + typeArgumentString + " test() { return null; }",
                        "}"
                ));
        CompilationSubject.assertThat(compilation).succeeded();
        return processor.getSafeName();
    }

    private static class SafeNameProcessor extends AbstractProcessor {

        private String safeName;

        String getSafeName() {
            assertThat(safeName).isNotNull();
            return safeName;
        }

        @Override
        public Set<String> getSupportedAnnotationTypes() {
            return ImmutableSet.of("*");
        }

        @Override
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.latestSupported();
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            if (roundEnv.processingOver()) {
                return true;
            }

            TypeElement typeElement = processingEnv.getElementUtils().getTypeElement("test.Test");
            assertThat(typeElement).isNotNull();

            List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());
            assertThat(methods).isNotEmpty();

            ExecutableElement testMethod = methods.get(0);

            TypeMirror returnType = testMethod.getReturnType();
            safeName = Names.safeName(returnType);

            return true;
        }
    }
}
