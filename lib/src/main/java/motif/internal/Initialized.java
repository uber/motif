package motif.internal;

public final class Initialized {
    public static final Object INITIALIZED = new Initialized();

    private Initialized() {
        // Private constructor to prevent instantiation
    }
}