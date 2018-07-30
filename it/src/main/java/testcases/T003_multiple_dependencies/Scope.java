package testcases.T003_multiple_dependencies;

@motif.Scope
public interface Scope {

    String string();

    @motif.Objects
    class Objects {

        String string(Integer i) {
            return "s" + i;
        }

        Integer integer() {
            return 1;
        }
    }
}
