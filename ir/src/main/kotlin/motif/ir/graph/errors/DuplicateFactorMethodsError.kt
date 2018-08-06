package motif.ir.graph.errors

import motif.ir.graph.DuplicateFactoryMethod

data class DuplicateFactorMethodsError(val duplicates: List<DuplicateFactoryMethod>)