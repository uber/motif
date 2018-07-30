package motif.compiler.names

class UniqueNameSet {

    private val used: MutableSet<String> = mutableSetOf()

    fun unique(base: String): String {
        var name = base
        var i = 2
        while (!used.add(name)) {
            name = "$base${i++}"
        }
        return name
    }

    fun claim(name: String) {
        used.add(name)
    }
}