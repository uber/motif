package motif.ast

interface IrAnnotationValue {
    /** The property name. */
    val name: String

    /**
     * The value set on the annotation property, or the default value if it was not explicitly set.
     *
     * Possible types are:
     * - Primitives (Boolean, Byte, Int, Long, Float, Double)
     * - String
     * - XEnumEntry
     * - XAnnotation
     * - XType
     * - List of [XAnnotationValue]
     */
    val value: Any?
}