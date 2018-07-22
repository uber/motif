package com.uber.motif.intellij.index

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.impl.source.JavaFileElementType
import com.intellij.psi.search.ProjectScopeBuilder
import com.intellij.util.indexing.*
import com.intellij.util.io.BooleanDataDescriptor
import com.uber.motif.intellij.psi.isMaybeScopeFile
import java.util.*

/**
 * Invoked during IntelliJ's indexing phase. The DataIndexer marks which files contain a Motif Scope. Once indexed,
 * other parts of the plugin can retrieve all Scope files via ScopeIndex.getSnapshot().
 */
class ScopeIndex : ScalarIndexExtension<Boolean>(), PsiDependentIndex {

    private val listeners: MutableSet<() -> Unit> = mutableSetOf()

    override fun getIndexer() = DataIndexer<Boolean, Void?, FileContent> { fileContent ->
        val isScopeFile = fileContent.psiFile.isMaybeScopeFile()
        // Since this method is triggered by DataIndexer.map, it fires before the index has been updated. Invoke
        // listeners asynchronously to ensure that they are called after the index is updated. Unclear whether this
        // is guaranteed by Application.invokeLater, but don't know of any better strategy.
        ApplicationManager.getApplication().invokeLater {
            notifyListeners()
        }
        mapOf(isScopeFile to null)
    }

    override fun getInputFilter() = object : DefaultFileTypeSpecificInputFilter(JavaFileType.INSTANCE) {

        override fun acceptInput(file: VirtualFile): Boolean {
            // All of the source code in the project. Would need to process more if we want to support Scopes provided
            // as a jar dependency.
            return JavaFileElementType.isInSourceContent(file)
        }
    }

    override fun getVersion() = Random().nextInt()
    override fun getName() = ID
    override fun dependsOnFileContent() = true
    override fun getKeyDescriptor() = BooleanDataDescriptor.INSTANCE!!

    @Synchronized
    fun registerListener(update: () -> Unit) {
        listeners.add(update)
        update()
    }

    @Synchronized
    private fun notifyListeners() {
        listeners.forEach { it() }
    }

    fun getSnapshot(project: Project): ScopeIndexSnapshot {
        val projectScopeBuilder = ProjectScopeBuilder.getInstance(project)
        val files: MutableList<VirtualFile> = mutableListOf()
        FileBasedIndex.getInstance().getFilesWithKey(ScopeIndex.ID, setOf(true), {
            files.add(it)
            true
        }, projectScopeBuilder.buildProjectScope())
        return ScopeIndexSnapshot(files)
    }

    fun refreshFile(project: Project, file: VirtualFile) {
        val projectScopeBuilder = ProjectScopeBuilder.getInstance(project)
        // processValues forces the file to be reindexed
        FileBasedIndex.getInstance().processValues(
                ScopeIndex.ID,
                true,
                file,
                { _, _ -> true },
                projectScopeBuilder.buildProjectScope())
    }

    companion object {

        private val ID: ID<Boolean, Void> = com.intellij.util.indexing.ID.create("ScopeIndex")

        fun getInstance(): ScopeIndex {
            return FileBasedIndexExtension.EXTENSION_POINT_NAME.findExtension(ScopeIndex::class.java)
                    ?: throw IllegalStateException("Could not find ScopeIndex. Make sure it is registered as a " +
                            "<fileBasedIndex> extension.")
        }
    }
}