package motif.daggercomparison.dagger;

import android.view.ViewGroup;
import dagger.BindsInstance;
import dagger.Provides;
import dagger.Subcomponent;

import javax.inject.Qualifier;

@LoggedInComponent.Scope
@Subcomponent(modules = LoggedInComponent.Module.class)
public interface LoggedInComponent {

    LoggedInController controller();

    @Scope
    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        Builder viewGroup(@LoggedIn ViewGroup parent);

        LoggedInComponent build();
    }

    @dagger.Module
    abstract class Module {

        @Scope
        @Provides
        static LoggedInView view(@LoggedIn ViewGroup parent) {
            return LoggedInView.create(parent);
        }
    }

    @javax.inject.Scope
    @interface Scope {}

    @Qualifier
    @interface LoggedIn {}
}
