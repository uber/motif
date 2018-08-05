package testcases.E003_scope_cycle;

@motif.Scope
public interface Scope {

    Scope child();
}
