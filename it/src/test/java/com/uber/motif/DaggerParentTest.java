package com.uber.motif;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.junit.Test;

import javax.inject.Named;

import static com.google.common.truth.Truth.assertThat;

public class DaggerParentTest {

    @Test
    public void daggerParent() {
        DaggerComponent daggerComponent = DaggerDaggerParentTest_DaggerComponent.create();
        MotifScope motifScope = new DaggerParentTest_MotifScopeImpl(daggerComponent);
        String string = motifScope.string();
        assertThat(string).isEqualTo("DaggerMotif");
    }

    @Scope
    interface MotifScope {

        String string();

        class Objects {

            public String string(@Named("Dagger") String dagger) {
                return dagger + "Motif";
            }
        }
    }

    @Component(modules = DaggerModule.class)
    interface DaggerComponent extends DaggerParentTest_MotifScopeImpl.Parent {}

    @Module
    static class DaggerModule {

        @Named("Dagger")
        @Provides
        String string() {
            return "Dagger";
        }
    }
}