package motif.cache

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ExtCacheScope(private val map: MutableMap<LazyExtGet, Any?> = mutableMapOf()) : MutableMap<LazyExtGet, Any?> by map

data class LazyExtGet(
        private val thisRef: Any?,
        private val property: KProperty<*>)

interface ExtCache {

    val cacheScope: ExtCacheScope

    fun <R, T> cache(block: R.() -> T): ReadOnlyProperty<R, T> {
        return object: ReadOnlyProperty<R, T> {

            @Synchronized
            override fun getValue(thisRef: R, property: KProperty<*>): T {
                val get = LazyExtGet(thisRef, property)
                @Suppress("UNCHECKED_CAST")
                return cacheScope.computeIfAbsent(get) { thisRef.block() } as T
            }
        }
    }
}
