/*
 * Copyright (c) 2022 Uber Technologies, Inc.
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
package com.uber.xprocessing.ext

import androidx.room.compiler.processing.XMethodElement
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XTypeElement
import androidx.room.compiler.processing.compat.XConverters.toJavac
import androidx.room.compiler.processing.compat.XConverters.toKS
import com.google.auto.common.MoreElements
import com.uber.xprocessing.ext.XOverrides.overrides

/**
 * A semi-port of [Overrides] from AutoCommon using the XProcessing APIs.
 * https://github.com/google/auto/blob/master/common/src/main/java/com/google/auto/common/Overrides.java
 *
 * Determines if one method overrides another. This class defines two ways of doing that:
 * [NativeOverrides] uses the method [Elements.overrides] while [ExplicitOverrides] reimplements
 * that method in a way that is more consistent between compilers, in particular between javac and
 * ecj (the Eclipse compiler).
 */
object XOverrides {
  fun overrides(
      overrider: XMethodElement,
      overridden: XMethodElement,
      inType: XTypeElement,
      env: XProcessingEnv,
      useMoreElements: Boolean = true,
  ): Boolean {
    if (useMoreElements) {
      return when (env.backend) {
        XProcessingEnv.Backend.JAVAC -> {
          MoreElements.overrides(
              overrider.toJavac(),
              overridden.toJavac(),
              inType.toJavac(),
              env.toJavac().typeUtils,
          )
          false
        }
        XProcessingEnv.Backend.KSP -> {
          env.resolver()?.overrides(overrider.toKS(), overridden.toKS(), inType.toKS()) ?: false
        }
      }
    }

    if (overrider.name != overridden.name) {
      // They must have the same name.
      return false
    }
    // We should just be able to write overrider.equals(overridden) here, but that runs afoul
    // of a problem with Eclipse. If for example you look at the method Stream<E> stream() in
    // Collection<E>, as obtained by collectionTypeElement.getEnclosedElements(), it will not
    // compare equal to the method Stream<E> stream() as obtained by
    // elementUtils.getAllMembers(listTypeElement), even though List<E> inherits the method
    // from Collection<E>. The reason is that, in ecj, getAllMembers does type substitution,
    // so the return type of stream() is Stream<E'>, where E' is the E from List<E> rather than
    // the one from Collection<E>. Instead we compare the enclosing element, which will be
    // Collection<E> no matter how we got the method. If two methods are in the same type
    // then it's impossible for one to override the other, regardless of whether they are the
    // same method.
    if (overrider.enclosingElement == overridden.enclosingElement) {
      return false
    }
    if (overridden.isStatic()) {
      // Static methods can't be overridden (though they can be hidden by other static methods).
      return false
    }
    val overriddenVisibility: Visibility = Visibility.of(overridden)
    val overriderVisibility: Visibility = Visibility.of(overrider)
    if (overridden.isPrivate() || overriderVisibility.compareTo(overriddenVisibility) < 0) {
      // Private methods can't be overridden, and methods can't be overridden by less-visible
      // methods. The latter condition is enforced by the compiler so in theory we might report
      // an "incorrect" result here for code that javac would not have allowed.
      return false
    }
    // TODO: do this right (added by me)
    if (overrider.parameters.size != overridden.parameters.size) {
      return false
    }
    for (i in overrider.parameters.indices) {
      if (!overrider.parameters[i].type.isSameType(overridden.parameters[i].type)) {
        return false
      }
    }
    val overriddenType = overridden.enclosingElement as? XTypeElement
    return if (inType.isClass()) {
      // Method mC in or inherited by class C (JLS 8.4.8.1)...
      if (overriddenType?.isClass() == true) {
        // ...overrides from C another method mA declared in class A. The only condition we
        // haven't checked is that C does not inherit mA. Ideally we could just write this:
        //    return !elementUtils.getAllMembers(in).contains(overridden);
        // But that doesn't work in Eclipse. For example, getAllMembers(AbstractList)
        // contains List.isEmpty() where you might reasonably expect it to contain
        // AbstractCollection.isEmpty(). So we need to visit superclasses until we reach
        // one that declares the same method, and check that we haven't reached mA. We compare
        // the enclosing elements rather than the methods themselves for the reason described
        // at the start of the method.
        false
      } else if (overriddenType?.isInterface() == true) {
        // ...overrides from C another method mI declared in interface I. We've already checked
        // the conditions (assuming that the only alternative to mI being abstract or default is
        // mI being static, which we eliminated above). However, it appears that the logic here
        // is necessary in order to be compatible with javac's `overrides` method. An inherited
        // abstract method does not override another method. (But, if it is not inherited,
        // it does, including if inType inherits a concrete method of the same name from its
        // superclass.) Here again we can use getAllMembers with javac but not with ecj. javac
        // says that getAllMembers(AbstractList) contains both AbstractCollection.size() and
        // List.size(), but ecj doesn't have the latter. The spec is not particularly clear so
        // either seems justifiable. So we need to look up the interface path that goes from inType
        // to `overriddenType` (or the several paths if there are several) and apply similar logic
        // to methodFromSuperclasses above.
        if (overrider.isAbstract()) {
          false
        } else {
          true
        }
      } else {
        // We don't know what this is so say no.
        false
      }
    } else {
      overriddenType?.isInterface() == true
      // Method mI in or inherited by interface I (JLS 9.4.1.1). We've already checked everything.
      // If this is not an interface then we don't know what it is so we say no.
    }
  }
}

enum class Visibility {
  PRIVATE,
  DEFAULT,
  PROTECTED,
  INTERNAL,
  PUBLIC,
  ;

  companion object {
    fun of(element: XMethodElement) =
        when {
          element.isPrivate() -> PRIVATE
          element.isProtected() -> PROTECTED
          element.returnType.isInternal() -> INTERNAL
          element.isPublic() -> PUBLIC
          else -> DEFAULT
        }
  }
}
