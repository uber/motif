package testcases.T026_child_dynamic_dependency;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    Child child(@Named("p") String parent);

    @motif.Objects
    class Objects {}

    @motif.Dependencies
    interface Dependencies {}
}
