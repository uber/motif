/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif;

import motif.internal.Constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ScopeFactory {

    private ScopeFactory() {}

    private static final NoDependencies NO_DEPENDENCIES = new NoDependencies() {};

    private static final Map<Class<?>, Constructor<?>> scopeClassToCreateMethod = new HashMap<>();

    public static <S extends Creatable<NoDependencies>> S create(Class<S> scopeClass) {
        return create(scopeClass, NO_DEPENDENCIES);
    }

    public static <S extends Creatable<D>, D> S create(Class<S> scopeClass, D dependencies) {
        Constructor<?> constructor = getConstructor(scopeClass);
        try {
            if (constructor.getParameterTypes().length == 0) {
                return (S) constructor.newInstance();
            } else {
                return (S) constructor.newInstance(dependencies);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized static Constructor<?> getConstructor(Class<?> scopeClass) {
        Constructor<?> constructor = scopeClassToCreateMethod.get(scopeClass);
        if (constructor == null) {
            Class<?> scopeImplClass = getScopeImplClass(scopeClass);
            constructor = scopeImplClass.getDeclaredConstructors()[0];
            scopeClassToCreateMethod.put(scopeClass, constructor);
        }
        return constructor;
    }

    private static Class<?> getScopeImplClass(Class<?> scopeClass) {
        String scopeImplClassName = getScopeImplClassName(scopeClass);
        try {
            return Class.forName(scopeImplClassName, true, scopeClass.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Could not find Scope implementation class " + scopeImplClassName + ". Ensure that the Motif " +
                            "annotation processor is enabled and that proguard is configured correctly (See README for details).");
        }
    }

    // Inspired by https://github.com/square/javapoet/issues/295
    private static String getScopeImplClassName(Class<?> scopeClass) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.SCOPE_IMPL_SUFFIX);
        Class<?> clazz = scopeClass;
        while (true) {
            sb.insert(0, clazz.getSimpleName());
            Class<?> enclosing = clazz.getEnclosingClass();
            if (enclosing == null) break;
            clazz = enclosing;
        }
        int lastDot = clazz.getName().lastIndexOf('.');
        if (lastDot != -1) {
            sb.insert(0, '.');
            sb.insert(0, clazz.getName().substring(0, lastDot));
        }
        return sb.toString();
    }
}
