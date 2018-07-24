package com.uber.motif;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.junit.Test;

import javax.inject.Named;

import static com.google.common.truth.Truth.assertThat;

public class DaggerInternalTest {

    @Test
    public void daggerInternal() {
        MotifScope motifScope = new DaggerInternalTest_MotifScopeImpl();
        assertThat(motifScope.string()).isEqualTo("DaggerAMotifDaggerBMotif");
    }

    @Scope
    interface MotifScope extends ComponentDependency {

        String string();

        class Objects {

            String string(@Named("DaggerA") String daggerA, @Named("DaggerB") String daggerB) {
                return daggerA + daggerB;
            }

            @Named("Motif")
            public String motif() {
                return "Motif";
            }

            @Spread
            DaggerComponent daggerComponent(MotifScope motifScope) {
                return DaggerDaggerInternalTest_DaggerComponent.builder()
                        .componentDependency(motifScope)
                        .build();
            }
        }
    }

    @Component(
            dependencies = ComponentDependency.class,
            modules = DaggerModule.class)
    interface DaggerComponent {

        @Named("DaggerA")
        String a();

        @Named("DaggerB")
        String b();
    }

    interface ComponentDependency {

        @Named("Motif")
        String motif();
    }

    @Module
    static class DaggerModule {

        @Named("DaggerA")
        @Provides
        String daggerA(@Named("Motif") String motif) {
            return "DaggerA" + motif;
        }

        @Named("DaggerB")
        @Provides
        String daggerB(@Named("Motif") String motif) {
            return "DaggerB" + motif;
        }
    }
}