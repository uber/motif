package motif.sample;
import motif.Scope;
@Scope (useNullFieldInitialization = true)
public interface MainJavaScope {
    Greeter greeter();

    @motif.Objects
    abstract class Objects {

        Greeter greeter() {
            return new Greeter("random");
        }
    }
}
