/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
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
package motif.ast.intellij

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.PsiElementFactory
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import motif.intellij.testing.InternalJdk
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType

class IntelliJKotlinTest : LightCodeInsightFixtureTestCase() {

    lateinit var psiElementFactory: PsiElementFactory

    override fun setUp() {
        super.setUp()

        psiElementFactory = PsiElementFactory.SERVICE.getInstance(project)
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return object : ProjectDescriptor(LanguageLevel.HIGHEST) {
            override fun getSdk() = InternalJdk.instance
        }
    }

    override fun getTestDataPath(): String {
        return "testData"
    }

    fun testImplicitNullabilityAnnotationType() {
        val fooPsiFile = myFixture.addFileToProject("Foo.kt", """
            open class Foo(val s: String) {
                
                internal fun a() {}
            }
        """.trimIndent()) as KtFile

        val fooPsiClass = fooPsiFile.declarations[0].toUElementOfType<UClass>()!!.javaPsi
        val psiAnnotation = fooPsiClass.constructors[0].parameterList.parameters[0].annotations[0]
        val annotation = IntelliJAnnotation(project, psiAnnotation)

        assertThat(annotation.type).isNull()
        assertThat(annotation.className).isEqualTo("org.jetbrains.annotations.NotNull")
    }
}