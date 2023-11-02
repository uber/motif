-libraryjars <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-dontshrink
-keep class **Test {
    public static void run();
}

-keepnames @motif.Scope interface *
-keepnames @motif.ScopeImpl class * {
    <init>(...);
}