package motif.compiler.errors

import motif.ir.source.dependencies.Dependencies

class MissingDependenciesError(val missingDependencies: Dependencies) : CompilationError("Missing dependencies: $missingDependencies")