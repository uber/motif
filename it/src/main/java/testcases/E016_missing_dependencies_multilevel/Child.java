package testcases.E016_missing_dependencies_multilevel;

@motif.Scope
public interface Child {

    Grandchild grandchild();

    String string();
}
