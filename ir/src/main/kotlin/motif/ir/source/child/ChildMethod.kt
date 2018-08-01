package motif.ir.source.child

import motif.ir.source.base.Dependency
import motif.ir.source.base.Type

class ChildMethod(
        val userData: Any?,
        val scope: Type,
        val dynamicDependencies: List<Dependency>)