package testcases.T011_inheritence_scope;

@com.uber.motif.Scope
public interface Scope extends ScopeParent<String, Integer> {

    class Objects {

        String string() {
            return "a";
        }

        Integer integer() {
            return 1;
        }
    }
}
