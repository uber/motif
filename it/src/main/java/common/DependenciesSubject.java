package common;

import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import motif.ir.source.base.Annotation;
import motif.ir.source.base.Dependency;
import motif.ir.source.base.Type;
import motif.ir.source.dependencies.AnnotatedDependency;
import motif.ir.source.dependencies.Dependencies;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertAbout;

public class DependenciesSubject extends Subject<DependenciesSubject, Dependencies> {

    private static final Subject.Factory<DependenciesSubject, Dependencies> FACTORY = DependenciesSubject::new;

    private final List<AnnotatedDependency> annotatedDependencies = new ArrayList<>();

    private DependenciesSubject(FailureMetadata metadata, @NullableDecl Dependencies actual) {
        super(metadata, actual);
    }

    public void matches() {
        assertThat(actual()).isNotNull();

        if (!equals(actual().getList(), annotatedDependencies)) {
            failWithActual("expected", annotatedDependencies);
        }
    }

    public DependenciesSubject with(Class<?> dependencyClass, Class<?>... consumingScopeClasses) {
        return _with(null, dependencyClass, consumingScopeClasses);
    }

    public DependenciesSubject with(String name, Class<?> dependencyClass, Class<?>... consumingScopeClasses) {
        return _with(name, dependencyClass, consumingScopeClasses);
    }

    private DependenciesSubject _with(@Nullable String name, Class<?> dependencyClass, Class<?>... consumingScopeClasses) {
        Annotation annotation = name == null
                ? null
                : new Annotation(null, "@Named(\"" + name + "\"");
        Set<Type> consumingScopes = Arrays.stream(consumingScopeClasses)
                .map(DependenciesSubject::type)
                .collect(Collectors.toSet());
        annotatedDependencies.add(
                new AnnotatedDependency(
                        new Dependency(null, type(dependencyClass), annotation),
                        false,
                        consumingScopes));
        return this;
    }

    private static Type type(Class<?> typeClass) {
        return new Type(null, typeClass.getName());
    }

    public static DependenciesSubject assertThat(Dependencies dependencies) {
        return assertAbout(FACTORY).that(dependencies);
    }

    private static boolean equals(List<AnnotatedDependency> actual, List<AnnotatedDependency> expected) {
        if (actual.size() != expected.size()) {
            return false;
        }

        for (int i = 0; i < actual.size(); i++) {
            if (!equals(actual.get(i), expected.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean equals(AnnotatedDependency actual, AnnotatedDependency expected) {
        if (!actual.getDependency().equals(expected.getDependency())) {
            return false;
        }

        if (!actual.getConsumingScopes().equals(expected.getConsumingScopes())) {
            return false;
        }

        return true;
    }
}
