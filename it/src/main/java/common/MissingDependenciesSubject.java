package common;

import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import motif.ir.graph.errors.MissingDependenciesError;
import motif.ir.source.base.Dependency;
import motif.ir.source.base.Type;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertAbout;

public class MissingDependenciesSubject extends Subject<MissingDependenciesSubject, MissingDependenciesError> {

    private static final Subject.Factory<MissingDependenciesSubject, MissingDependenciesError> FACTORY = MissingDependenciesSubject::new;

    private MissingDependenciesSubject(FailureMetadata metadata, @NullableDecl MissingDependenciesError actual) {
        super(metadata, actual);
    }

    public void matches(Class<?> scopeClass, Class<?>... dependencies) {
        assertThat(actual()).isNotNull();

        Type expectedScopeType = type(scopeClass);
        Type actualScopeType = actual().getRequiredBy().getScopeClass().getType();
        if (!expectedScopeType.equals(actualScopeType)) {
            failWithoutActual(
                    Fact.fact("expected Scope", expectedScopeType),
                    Fact.fact("but was", actualScopeType));
        }

        List<Dependency> expectedDependencies = Arrays.stream(dependencies)
                .map(expectedDependencyClass -> new Dependency(null, type(expectedDependencyClass), null))
                .collect(Collectors.toList());
        Truth.assertThat(actual().getDependencies()).containsExactlyElementsIn(expectedDependencies);
    }

    private static Type type(Class<?> typeClass) {
        return new Type(null, typeClass.getName());
    }

    public static MissingDependenciesSubject assertThat(MissingDependenciesError error) {
        return assertAbout(FACTORY).that(error);
    }
}
