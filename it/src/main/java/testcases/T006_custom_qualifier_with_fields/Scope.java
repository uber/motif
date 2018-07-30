package testcases.T006_custom_qualifier_with_fields;

@motif.Scope
public interface Scope {

    String string();

    @motif.Objects
    class Objects {

        String s(
                @CustomQualifier(first = "1", second = "2") String a,
                @CustomQualifier(first = "3", second = "4") String b) {
            return a + b;
        }

        @CustomQualifier(first = "1", second = "2")
        String a() {
            return "a";
        }

        @CustomQualifier(first = "3", second = "4")
        String b() {
            return "b";
        }
    }
}
