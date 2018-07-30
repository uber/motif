package motif.util

class MultiMap<K, V> {

    private val map: MutableMap<K, MutableSet<V>> = mutableMapOf()
}