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

    @Override
    public String toString() {
        return "has debug string";
    }
}
