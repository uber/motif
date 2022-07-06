-libraryjars <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-optimizations !class/merging/*
-dontshrink
-keep class **Test {
    public static void run();
}