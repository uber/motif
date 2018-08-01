package motif.ir.source.base

class Annotation(
        val userData: Any?,
        val id: String) {

    override fun toString(): String {
        return id
    }

    override fun equals(other: Any?): Boolean {
        val o = other as? Annotation ?: return false
        return id == o.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}