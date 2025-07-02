package motif.sample;
import javax.inject.Singleton;

import motif.Scope;

@Scope
public interface MainJavaScope {
    Greeter greeter();

    @motif.Objects
    abstract class Objects {

        Greeter greeter() {
            return new Greeter("random");
        }
    }
}
