package testcases.T012_inheritence_objects;

import javax.inject.Named;

public abstract class ObjectsGrandparent<TT> {

    abstract TT grandparent();

    @Named("grandparent")
    String stringGrandparent() {
        return "grandparent";
    }
}
