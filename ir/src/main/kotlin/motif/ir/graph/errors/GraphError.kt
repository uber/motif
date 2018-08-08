package motif.ir.graph.errors

interface GraphError {

    // Intellij seems to truncate any content after the first '\n' character.
    val message: String
}