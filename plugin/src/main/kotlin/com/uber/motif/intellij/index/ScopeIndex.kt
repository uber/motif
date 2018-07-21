package com.uber.motif.intellij.index

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.JavaFileElementType
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import java.io.DataInput
import java.io.DataOutput

class ScopeIndex : FileBasedIndexExtension<String, String>() {

    override fun getIndexer() = DataIndexer<String, String, FileContent> { fileContent ->
        mutableMapOf<String, String>()
    }

    override fun getInputFilter() = object : DefaultFileTypeSpecificInputFilter(JavaFileType.INSTANCE) {

        override fun acceptInput(file: VirtualFile): Boolean {
            // All of the source code in the project. Would need to process more if we want to support Scopes provided
            // as a jar dependency.
            return JavaFileElementType.isInSourceContent(file)
        }
    }

    override fun getValueExternalizer() = object : DataExternalizer<String> {
        override fun save(out: DataOutput, value: String) {
            out.writeUTF(value)
        }

        override fun read(`in`: DataInput): String {
            return `in`.readUTF()
        }
    }

    override fun getVersion() = 1
    override fun getName() = ID
    override fun dependsOnFileContent() = true
    override fun getKeyDescriptor() = EnumeratorStringDescriptor()

    companion object {
        val ID: ID<String, String> = com.intellij.util.indexing.ID.create("ScopeIndex")
    }
}