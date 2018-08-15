package motif.daggercomparison.dagger;

import android.view.ViewGroup;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Provides;

import javax.inject.Qualifier;

@RootComponent.Scope
@Component(modules = RootComponent.Module.class)
public interface RootComponent {

    RootController controller();

    LoggedInComponent.Builder loggedIn();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder viewGroup(@Root ViewGroup parent);

        RootComponent build();
    }

    @dagger.Module
    abstract class Module {

        @Scope
        @Provides
        static RootView view(@Root ViewGroup parent) {
            return RootView.create(parent);
        }
    }

    @javax.inject.Scope
    @interface Scope {}

    @Qualifier
    @interface Root {}
}
