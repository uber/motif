package testcases.T018_spread_inheritence;

import javax.inject.Named;

public class SpreadableParent {

    @Named("a")
    public String a() {
        return "a";
    }

    @Named("b")
    public String b() {
        return "b";
    }
}
