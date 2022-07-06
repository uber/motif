API for rendering `MotifErrors` into human readable text.

**Usage**

```kotlin
val graph: ResolvedGraph = getGraph()
val error: MotifError = getError()
val message: String = ErrorMessage.get(graph, error)
```
