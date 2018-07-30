package common;

import com.google.common.truth.Correspondence;
import com.uber.motif.compiler.model.Dependency;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class DebugStringCorrespondence extends Correspondence<Dependency, String> {

    @Override
    public boolean compare(@NullableDecl Dependency actual, @NullableDecl String expected) {
        if (actual == null) {
            return false;
        }
        return actual.getDebugString().equals(expected);
    }

    @NullableDecl
    @Override
    public String formatDiff(@NullableDecl Dependency actual, @NullableDecl String expected) {
        String actualString = actual == null ? "null" : actual.getDebugString();
        return actualString + " != " + expected;
    }

    @Override
    public String toString() {
        return "has debug string";
    }
}
