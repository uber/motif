package motif.ir.graph

import motif.ir.source.objects.FactoryMethod

data class DuplicateFactoryMethod(val duplicate: FactoryMethod, val existing: List<FactoryMethod>)