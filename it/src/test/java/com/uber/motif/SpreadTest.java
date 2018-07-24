package com.uber.motif;

import org.junit.Test;

import javax.inject.Named;

import static com.google.common.truth.Truth.assertThat;

public class SpreadTest {

    @Test
    public void spread() {
        Scope scope = new SpreadTest_FactoryImpl().scope(new Spreadable());
        assertThat(scope.string()).isEqualTo("AB");
    }

    @com.uber.motif.Scope
    interface Factory {

        Scope scope(@Spread Spreadable spreadable);
    }

    @com.uber.motif.Scope
    interface Scope {

        String string();

        class Objects {

            String string(@Named("A") String a, @Named("B") String b) {
                return a + b;
            }
        }
    }

    class Spreadable {

        @Named("A")
        String a() {
            return "A";
        }

        @Named("B")
        String b() {
            return "B";
        }
    }
}
