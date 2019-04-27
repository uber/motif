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
package motif.compiler;

import com.google.common.collect.ImmutableSet;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.JavaFileObjects;
import dagger.shaded.auto.common.AnnotationMirrors;
import motif.compiler.Names;
import org.junit.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Qualifier;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class NamesTest {

    @Test
    public void basic() {
        String name = getSafeName("java.util.HashMap");
        assertThat(name).isEqualTo("hashMap");
    }

    @Test
    public void typeArgument() {
        String name = getSafeName("java.util.HashMap<String, Integer>");
        assertThat(name).isEqualTo("stringIntegerHashMap");
    }

    @Test
    public void wildcard() {
        String name = getSafeName("java.util.HashMap<? extends String, ? super Integer>");
        assertThat(name).isEqualTo("stringIntegerHashMap");
    }

    @Test
    public void typeVariable() {
        String name = getSafeName("java.util.HashMap<String, A>");
        assertThat(name).isEqualTo("stringAHashMap");
    }

    @Test
    public void nested() {
        String name = getSafeName("java.util.HashMap<java.util.HashMap<String, Integer>, Integer>");
        assertThat(name).isEqualTo("stringIntegerHashMapIntegerHashMap");
    }

    @Test
    public void innerClass() {
        String name = getSafeName("java.util.Map.Entry<String, Integer>");
        assertThat(name).isEqualTo("stringIntegerMapEntry");
    }

    @Test
    public void keyword() {
        String name = getSafeName("java.lang.Boolean");
        assertThat(name).isEqualTo("boolean_");
    }

    @Test
    public void named() {
        String name = getSafeName("@javax.inject.Named(\"Foo\") String");
        assertThat(name).isEqualTo("fooString");
    }

    @Test
    public void customQualifier() {
        String name = getSafeName("@Foo String");
        assertThat(name).isEqualTo("fooString");
    }

    private static String getSafeName(String classString) {
        SafeNameProcessor processor = new SafeNameProcessor();
        Compilation compilation = javac()
                .withProcessors(processor)
                .compile(JavaFileObjects.forSourceLines(
                        "test.Test",
                        "package test;",
                        "@javax.inject.Qualifier @interface Foo {}",
                        "class Test<A extends String> {",
                        classString + " test() { return null; }",
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
            Collection<? extends AnnotationMirror> qualifiers = AnnotationMirrors.getAnnotatedAnnotations(testMethod, Qualifier.class);
            AnnotationMirror qualifier = qualifiers.isEmpty() ? null : qualifiers.iterator().next();
            safeName = Names.safeName(returnType, qualifier);

            return true;
        }
    }
}
