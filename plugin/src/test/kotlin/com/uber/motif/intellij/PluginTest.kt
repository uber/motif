package com.uber.motif.intellij

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.builders.EmptyModuleFixtureBuilder
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.junit.Test


class PluginTest : UsefulTestCase() {

    private lateinit var psiDocumentManager: PsiDocumentManager
    private lateinit var fixture: CodeInsightTestFixture
    private lateinit var project: Project
    private lateinit var component: MotifComponent

    @Test
    fun test() {
        val testPsiFile: PsiFile = fixture.configureByFiles(
                "src/a/Test.java",
                "src/com/uber/motif/Scope.java")[0]
        val testDocument: Document = psiDocumentManager.getDocument(testPsiFile)!!

        println(component.graphProcessor.scopeClassesMap())

        fixture.doHighlighting()

        val lineMarkers: List<LineMarkerInfo<PsiElement>> = DaemonCodeAnalyzerImpl.getLineMarkers(testDocument, project)
        println(lineMarkers)
    }

    override fun setUp() {
        super.setUp()

        val projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(javaClass.name + "." + name)
        fixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.fixture)

        val moduleFixtureBuilder = projectBuilder.addModule(EmptyModuleFixtureBuilder::class.java)
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