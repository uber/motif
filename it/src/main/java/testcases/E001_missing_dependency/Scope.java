package testcases.E001_missing_dependency;

@com.uber.motif.Scope
public interface Scope {

    String string();

    interface Parent {}
}
