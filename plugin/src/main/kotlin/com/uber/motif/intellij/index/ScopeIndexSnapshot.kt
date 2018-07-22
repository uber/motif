package com.uber.motif.intellij.index

import com.intellij.openapi.vfs.VirtualFile

class ScopeIndexSnapshot(val files: List<VirtualFile>) {

    override fun toString(): String {
        return "ScopeIndexSnapshot[${files.size}]"
    }
}