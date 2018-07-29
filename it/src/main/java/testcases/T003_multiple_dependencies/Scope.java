package testcases.T003_multiple_dependencies;

@com.uber.motif.Scope
public interface Scope {

    String string();

    class Objects {

        String string(Integer i) {
            return "s" + i;
        }

        Integer integer() {
            return 1;
        }
    }
}
