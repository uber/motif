package testcases.T010_dependency_cache_donotcache;

import com.uber.motif.DoNotCache;

import javax.inject.Named;

@com.uber.motif.Scope
public interface Scope {

    String string();

    class Objects {

        int i = 0;

        String string(@Named("a") String a, @Named("i") String i) {
            return a + i;
        }

        @Named("a")
        String a(@Named("i") String i1, @Named("i") String i2) {
            return "a" + i1 + i2;
        }

        @DoNotCache
        @Named("i")
        String i() {
            return String.valueOf(i++);
        }
    }
}
