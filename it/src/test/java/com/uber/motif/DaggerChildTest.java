package com.uber.motif;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.junit.Test;

import javax.inject.Named;

import static com.google.common.truth.Truth.assertThat;

public class DaggerChildTest {

    @Test
    public void daggerChild() {
        MotifScope motifScope = new DaggerChildTest_MotifScopeImpl();
        DaggerComponent daggerComponent = DaggerDaggerChildTest_DaggerComponent.builder()
                .componentDependency(motifScope)
                .build();
        assertThat(daggerComponent.string()).isEqualTo("DaggerMotif");
    }

    @Scope
    interface MotifScope extends ComponentDependency {

        class Objects {

            @Named("Motif")
            public String string() {
                return "Motif";
            }
        }
    }

    @Component(
            dependencies = ComponentDependency.class,
            modules = DaggerModule.class)
    interface DaggerComponent {

        String string();
    }

    interface ComponentDependency {

        @Named("Motif")
        String string();
    }

    @Module
    static class DaggerModule {

        @Provides
        String string(@Named("Motif") String motif) {
            return "Dagger" + motif;
        }
    }
}