package com.uber.motif;

import org.junit.Test;

import javax.inject.Named;

import static com.google.common.truth.Truth.assertThat;

public class SpreadTest {

    @Test
    public void spread() {
        Scope scope = new SpreadTest_FactoryImpl().scope(new Spreadable1());
        assertThat(scope.string()).isEqualTo("ABCD");
    }

    @com.uber.motif.Scope
    interface Factory {

        Scope scope(@Spread Spreadable1 spreadable);

        interface Parent {}
    }

    @com.uber.motif.Scope
    interface Scope {

        String string();

        abstract class Objects {

            @Spread
            abstract Spreadable2 spreadable2();

            String string(
                    @Named("A") String a,
                    @Named("B") String b,
                    @Named("C") String c,
                    @Named("D") String d) {
                return a + b + c + d;
            }
        }
    }

    static class Spreadable1 {

        @Named("A")
        public String a() {
            return "A";
        }

        @Named("B")
        public String b() {
            return "B";
        }
    }

    static class Spreadable2 {

        @Named("C")
        public String c() {
            return "C";
        }

        @Named("D")
        public String d() {
            return "D";
        }
    }
}
