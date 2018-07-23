package com.uber.motif.intellij

import com.intellij.openapi.project.Project
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.builders.EmptyModuleFixtureBuilder
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.junit.Test


class PluginTest : UsefulTestCase() {

    private lateinit var fixture: CodeInsightTestFixture
    private lateinit var project: Project
    private lateinit var component: MotifComponent

    @Test
    fun test() {
        fixture.copyDirectoryToProject("src", ".")

        CodeInsightTestFixtureImpl.ensureIndexesUpToDate(fixture.project)

        println(component.graphProcessor.scopeClasses())
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
    }

    override fun tearDown() {
        try {
            fixture.tearDown()
        } finally {
            super.tearDown()
        }
    }
}