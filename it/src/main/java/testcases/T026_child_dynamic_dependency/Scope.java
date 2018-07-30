package testcases.T026_child_dynamic_dependency;

import javax.inject.Named;

@com.uber.motif.Scope
public interface Scope {

    Child child(@Named("p") String parent);

    class Objects {}
}
