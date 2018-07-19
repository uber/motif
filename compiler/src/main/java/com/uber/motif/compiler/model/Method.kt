package com.uber.motif.compiler.model

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType


interface Method {

    // Hack until https://github.com/square/javapoet/issues/656 is resolved
    val env: ProcessingEnvironment

    val owner: DeclaredType
    val method: ExecutableElement
    val methodType: ExecutableType
}