package motif.ir.source.base

class Dependency(
        val userData: Any?,
        val type: Type,
        val qualifier: Annotation?) : Comparable<Dependency> {

    private val key = Key(type, qualifier)

    private val compareKey: String by lazy {
        "${type.id}-${qualifier?.id}"
    }

    override fun compareTo(other: Dependency): Int {
        return compareKey.compareTo(other.compareKey)
    }

    override fun toString(): String {
        val qualifierString = qualifier?.let { "$it " } ?: ""
        return "$qualifierString\n${type.simpleName}"
    }

    override fun equals(other: Any?): Boolean {
        val o = other as? Dependency ?: return false
        return key == o.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}

private data class Key(val type: Type, val qualifier: Annotation?)