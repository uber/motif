package com.uber.motif.source

enum class SourceType {

    // Scope Class

    SCOPE_CLASS,

    // Dependency Interface

    DEPENDENCIES_INTERFACE,
    SCOPE_DEPENDENCY_METHOD,

    // Child Declarations

    CHILD_DECLARATION,
    CHILD_IMPLEMENTATION,
    CHILD_METHOD,
    CHILD_PARAMETER,

    // Expose Methods

    EXPOSE_METHOD,

    // Object Class

    OBJECT_CLASS,

    // Provider Methods

    BASIC_PROVIDER_METHOD,
    BASIC_PROVIDER_PARAMETER,

    CONSTRUCTOR_PROVIDER_METHOD,
    CONSTRUCTOR_PROVIDER_PARAMETER,

    BINDS_PROVIDER_METHOD,
    BINDS_PROVIDER_PARAMETER,
}