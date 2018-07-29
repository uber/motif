package testcases.T006_custom_qualifier_with_fields;

import javax.inject.Qualifier;

@Qualifier
public @interface CustomQualifier {

    String first();

    String second();
}
