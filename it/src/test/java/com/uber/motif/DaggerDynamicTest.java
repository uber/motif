package com.uber.motif;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.junit.Test;

import javax.inject.Named;

import static com.google.common.truth.Truth.assertThat;

public class DaggerDynamicTest {

    @Test
    public void daggerDynamic() {
        DaggerComponent daggerComponent = DaggerDaggerDynamicTest_DaggerComponent.create();
        MotifScope motifScope = new DaggerDynamicTest_MotifScopeImpl();
        MotifChild motifChild = motifScope.child(daggerComponent);
        assertThat(motifChild.string()).isEqualTo("DaggerMotif");
    }

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