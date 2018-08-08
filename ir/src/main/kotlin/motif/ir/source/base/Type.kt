package motif.ir.source.base

class Type(val userData: Any?, val id: String) {

    val packageName: String by lazy {
        id.substringBeforeLast('.')
    }

    val simpleName: String by lazy {
        id.substringAfterLast('.')
    }

    override fun toString(): String {
        return id
    }

    override fun equals(other: Any?): Boolean {
        val o = other as? Type ?: return false
        return id == o.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}