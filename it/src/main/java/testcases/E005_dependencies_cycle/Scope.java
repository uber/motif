package testcases.E005_dependencies_cycle;

@motif.Scope
public interface Scope {

    @motif.Objects
    class Objects {

        String string(Integer i) {
            return String.valueOf(i);
        }

        Integer integer(String s) {
            return Integer.parseInt(s);
        }
    }
}
