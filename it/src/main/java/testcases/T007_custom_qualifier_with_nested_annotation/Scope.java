package testcases.T007_custom_qualifier_with_nested_annotation;

@com.uber.motif.Scope
public interface Scope {

    String string();

    class Objects {

        String s(
                @CustomQualifier(field = @Field("a")) String a,
                @CustomQualifier(field = @Field("b")) String b) {
            return a + b;
        }

        @CustomQualifier(field = @Field("a"))
        String a() {
            return "a";
        }

        @CustomQualifier(field = @Field("b"))
        String b() {
            return "b";
        }
    }
}
