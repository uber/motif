package com.uber.motif.sample;

import com.uber.motif.Scope;
import com.uber.motif.Spread;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;

class DaggerDynamic {

    @Scope
    interface MotifScope {

        MotifChild child(@Spread DaggerComponent component);

        class Objects {

            @Named("Motif")
            public String string() {
                return "Motif";
            }
        }

        interface Parent {}
    }

    @Scope
    interface MotifChild {

        String string();

        class Objects {

            String string(@Named("Dagger") String dagger, @Named("Motif") String motif) {
                return dagger + motif;
            }
        }
    }

    @Component(modules = DaggerModule.class)
    interface DaggerComponent {

        @Named("Dagger")
        String string();
    }

    @Module
    static class DaggerModule {

        @Named("Dagger")
        @Provides
        String string() {
            return "Dagger";
        }
    }
}