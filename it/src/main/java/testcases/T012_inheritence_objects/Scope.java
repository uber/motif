package testcases.T012_inheritence_objects;

import javax.inject.Named;

@com.uber.motif.Scope
public interface Scope {

    A a();
    B b();

    @Named("parent")
    String parent();

    @Named("grandparent")
    String grandparent();

    abstract class Objects extends ObjectsParent<A, B> {}
}
