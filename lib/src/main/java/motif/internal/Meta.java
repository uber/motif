package motif.internal;

public @interface Meta {
    boolean transitive();
    Class<?>[] consumingScopes();
}
