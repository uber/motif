package testcases.T012_inheritence_objects;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    A a();
    B b();

    @Named("parent")
    String parent();

    @Named("grandparent")
    String grandparent();

    @motif.Objects
    abstract class Objects extends ObjectsParent<A, B> {}

    @motif.Dependencies
    interface Dependencies {}
}
