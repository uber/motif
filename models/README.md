AST model representation of [Motif concepts](https://github.com/uber/motif/wiki).

**Usage**

```kotlin
val scopeClasses: List<IrClasses> = getScopeClasses()
val scopes: List<Scope> = Scope.fromClasses(scopeClasses)
```
