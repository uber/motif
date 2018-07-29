package testcases.T017_spread;

import javax.inject.Named;

public class Spreadable {
    @Named("a")
    public String a() {
        return "a";
    }

    @Named("b")
    public String b() {
        return "b";
    }
}
