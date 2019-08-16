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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This API in in beta.
 *
 * https://github.com/uber/motif/issues/108
 *
 * @param <S> Scope class
 * @param <D> Dependencies interface
 */
@ScopeFactoryMarker
public class ScopeFactory<S, D> {

    private static final Map<Class<?>, Method> scopeFactoryClassToHelperCreateMethod = new HashMap<>();

    public S create(D dependencies) {
        Method createMethod = getFactoryHelperCreateMethod(getClass());
        try {
            return (S) createMethod.invoke(null, dependencies);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized static Method getFactoryHelperCreateMethod(Class<?> scopeFactoryClass) {
        Method createMethod = scopeFactoryClassToHelperCreateMethod.get(scopeFactoryClass);
        if (createMethod == null) {
            Class<?> factoryHelperClass = getFactoryHelperClass(scopeFactoryClass);
            createMethod = factoryHelperClass.getDeclaredMethods()[0];
            scopeFactoryClassToHelperCreateMethod.put(scopeFactoryClass, createMethod);
        }
        return createMethod;
    }

    private static Class<?> getFactoryHelperClass(Class<?> scopeFactoryClass) {
        String helperClassName = getHelperClassName(scopeFactoryClass);
        try {
            return Class.forName(helperClassName, false, scopeFactoryClass.getClassLoader());
        } catch (ClassNotFoundException e) {
            if (scopeFactoryClass.isAnonymousClass()) {
                throw new RuntimeException("Anonymous ScopeFactory classes are not supported.");
            } else {
                throw new RuntimeException("Could not find generated helper class " + helperClassName + ". Ensure " +
                        "that the Motif annotation processor is enabled.");
            }
        }
    }

    // https://github.com/square/javapoet/issues/295
    private static String getHelperClassName(Class<?> scopeFactoryClass) {
        StringBuilder sb = new StringBuilder();
        sb.append("Helper");
        Class<?> clazz = scopeFactoryClass;
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
