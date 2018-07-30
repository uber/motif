package testcases.T026_child_dynamic_dependency;

import javax.inject.Named;

@com.uber.motif.Scope
public interface Child {

    @Named("c")
    String string();

    class Objects {

        @Named("c")
        String string(@Named("p") String p) {
            return "c" + p;
        }
    }
}
