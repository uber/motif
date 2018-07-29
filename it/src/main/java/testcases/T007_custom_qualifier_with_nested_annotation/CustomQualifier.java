package testcases.T007_custom_qualifier_with_nested_annotation;

import javax.inject.Qualifier;

@Qualifier
public @interface CustomQualifier {

    Field field();
}
