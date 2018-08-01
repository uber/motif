package com.uber.motif.intellij

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.builders.EmptyModuleFixtureBuilder
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory
import motif.intellij.MotifComponent
import org.junit.Test
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis


class PluginTest : UsefulTestCase() {

    private lateinit var psiDocumentManager: PsiDocumentManager
    private lateinit var fixture: CodeInsightTestFixture
    private lateinit var project: Project
    private lateinit var component: MotifComponent

    @Test
    fun test() {
        val testPsiFiles: Array<PsiFile> = fixture.configureByFiles(
                "src/a/Test.java",
                "src/a/Test2.java",
                "src/a/Test3.java",
                "src/a/Test4.java",
                "src/com/uber/motif/Scope.java")
        val testDocument: Document = psiDocumentManager.getDocument(testPsiFiles[0])!!
        testPsiFiles.map { it as PsiJavaFile }.forEach { file ->
            val psiClass = file.classes[0]
            println("NANO: $psiClass ========")
            println(measureNanoTime {
                psiClass.visibleSignatures
            })
            println(measureNanoTime {
                psiClass.visibleSignatures
            })
            println(measureNanoTime {
                psiClass.visibleSignatures
            })
        }

        fixture.doHighlighting()

        val lineMarkers: List<LineMarkerInfo<PsiElement>> = DaemonCodeAnalyzerImpl.getLineMarkers(testDocument, project)
        println(lineMarkers)
    }

    override fun setUp() {
        super.setUp()

        val projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(javaClass.name + "." + name)
        fixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.fixture)

        val moduleFixtureBuilder = projectBuilder.addModule(JavaModuleFixtureBuilder::class.java)
                .addJdk("/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home")
        moduleFixtureBuilder.addSourceContentRoot(fixture.tempDirPath)

        fixture.setUp()
        fixture.testDataPath = "src/test/testdata"

        project = fixture.project

        component = MotifComponent.get(project)
        psiDocumentManager = PsiDocumentManager.getInstance(project)
    }

    override fun tearDown() {
        try {
            fixture.tearDown()
        } finally {
            super.tearDown()
        }
    }
}