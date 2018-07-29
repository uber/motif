package testcases.T012_inheritence_objects;

import javax.inject.Named;

public abstract class ObjectsParent<T, TT> extends ObjectsGrandparent<TT> {

    abstract T parent();

    @Named("parent")
    String stringParent() {
        return "parent";
    }
}
