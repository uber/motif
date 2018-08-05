package motif.ir.graph

import motif.ir.source.ScopeClass
import motif.ir.source.objects.FactoryMethod

class DependencyCycle(val scopeClass: ScopeClass, val cycle: List<FactoryMethod>)