# Motif

[![Build Status](https://travis-ci.com/uber/motif.svg?branch=master)](https://travis-ci.com/uber/motif)

Motif is a DI library that offers a simple API optimized for nested scopes.

*Note: Past versions of Motif generated Dagger code under the hood. This is no longer the case.*

## Other Resources

* [Dagger Interoperability](https://github.com/uber/motif/blob/master/DAGGER.md)
* [IDE Integration](https://github.com/uber/motif/tree/master/intellij)
* [DroidCon Talk](https://youtu.be/Y45MqYNjts0)

## Gradle

| [![Maven Central](https://img.shields.io/maven-central/v/com.uber.motif/motif-compiler.svg)](https://search.maven.org/artifact/com.uber.motif/motif-compiler)<br>[![Maven Central](https://img.shields.io/maven-central/v/com.uber.motif/motif.svg)](https://search.maven.org/artifact/com.uber.motif/motif) | <pre>annotationProcessor 'com.uber.motif:motif-compiler:x.y.z'<br>implementation 'com.uber.motif:motif:x.y.z'</pre> |
|-|:-|

## Proguard

```proguard
-keep class motif.Scope
-keep class motif.ScopeImpl
-keep @motif.Scope interface *
-keep @motif.ScopeImpl class *
```

## The Basics

This is a Motif Scope. It serves as a container for objects that can be created by this Scope:

<details>
<summary>Notes for Dagger users...</summary>

A Motif Scope is analogous to a Dagger `@Component`.
</details>

```java
@motif.Scope
interface MainScope {}
```

Define a `@motif.Objects`-annotated class to hold *factory methods*, which tell Motif how to create objects.

<details>
<summary>Notes for Dagger users...</summary>

The nested `Objects` class is just like a Dagger `@Module` except Motif only allows you to define one `Objects` class per Scope. Factory methods are analogous to `@Provides` methods.
</details>

```java
@motif.Scope
interface MainScope {

    @motif.Objects
    class Objects {

        Controller controller() {
            return new Controller();
        }
    }
}
```

Pass object dependencies as factory method parameters. Motif must know how to create dependencies as well:

```java
@motif.Scope
interface MainScope {

    @motif.Objects
    class Objects {

        View view() {
            return new View();
        }
        
        Database database() {
            return new Database();
        }

        Controller controller(View view, Database database) {
            return new Controller(view, database);
        }
    }
}
```

Retrieve objects from a Scope via *access methods* defined on your Scope interface:

<details>
<summary>Notes for Dagger users...</summary>

Access methods are analogous to a Dagger `@Component` [provision methods](https://google.github.io/dagger/api/2.14/dagger/Component.html#provision-methods).
</details>

```java
@motif.Scope
interface MainScope {

    Controller controller();

    @motif.Objects
    class Objects {

        View view() {
            return new View();
        }
        
        Database database() {
            return new Database();
        }

        Controller controller(View view, Database database) {
            return new Controller(view, database);
        }
    }
}
```

At build time, Motif generates an implementation class for each scope:

```java
MainScope mainScope = new MainScopeImpl();
Controller controller = mainScope.controller();
```

## Child Scopes

Define a *child method* on the Scope interface to declare a Scope as the child of another Scope:

<details>
<summary>Notes for Dagger users...</summary>

This is similar to a Dagger `@Subcomponent` [factory method](https://google.github.io/dagger/api/2.14/dagger/Component.html#subcomponents) on a parent `@Component`.
</details>

```java
@motif.Scope
interface MainScope {

    ChildScope child();
    
    // ...
}
```

Annotate a factory method with `@Expose` to make it visible to child Scopes:

<details>
<summary>Notes for Dagger users...</summary>

Unlike Dagger `@Subcomponents` which expose all objects down the graph by default, Motif Scopes consider objects internal to the Scope unless explicitly annotated otherwise.
</details>

```java
@motif.Scope
interface MainScope {

    ChildScope child();

    // ...

    @motif.Objects
    class Objects {

        @Expose
        Database database() {
            return new Database();
        }

        // ...
    }
}

@motif.Scope
interface ChildScope {

    ChildController controller();

    @motif.Objects
    class Objects {

        // No Database factory method. Child Controller receives the Database defined by MainScope.

        ChildView view() {
            return new ChildView();
        }

        ChildController controller(Database database, ChildView view) {
            return new ChildController(database, view);
        }
    }
}
```

Create an instance of a child Scope by calling the parent's child method:

```java
MainScope mainScope = new MainScopeImpl();
ChildScope childScope = mainScope.child();
```

## Root Scopes

By extending `Creatable<D>` you can specify exactly the dependencies you expect from the parent `Scope`. This allows
Motif to report missing dependencies at compile time.

```java
@motif.Scope
interface MainScope extends Creatable<MainDependencies> {

    // ...
}

interface MainDependencies {}
```

Extending `Creatable<D>` also enables instantiation of a "root" `Scope` without referencing generated code using Motif's `ScopeFactory.create` API:

```java
MainDependencies dependencies = ...;
MainScope mainScope = ScopeFactory.create(MainScope.class, dependencies)
```

## Convenience APIs

Factory methods that pass parameters through to a constructor without modification can be converted to parameterless abstract methods:

<details>
<summary>Notes for Dagger users...</summary>

This feature is similar to Dagger's `@Inject` constructor injection, but it only requires annotating the class' constructor if there are multiple constructors, and it scopes the object to the enclosing Motif Scope.
</details>

```java
@motif.Scope
interface MainScope {

    // ...

    @motif.Objects
    abstract class Objects {
        abstract View view();
        abstract Database database();
        abstract Controller controller();
    }
}
```

Motif understands inheritence and generics as well:

```java
interface ControllerObjects<C, V> {
    V view();
    C controller();
}

@motif.Scope
interface MainScope {

    // ...

    @motif.Objects
    abstract class Objects implements ControllerObjects<Controller, View> {
        abstract Database database();
    }
}
```

## Motif vs Dagger

* Related: [Dagger Interoperability](https://github.com/uber/motif/blob/master/DAGGER.md)

Motif sacrifices flexibility in favor of an opinionated API optimized specifically for deep DI scope hierarchies (ie. many levels of nested `@Components` or `@Subcomponents`). In these cases, Motif aims to minimize initial development cost and continued conceptual overhead attributed to DI configuration by offering a simple, targeted API. Dagger can express everything that Motif can and much more, but Dagger's greater flexibility requires many more concepts to be understood by the developer, increases verbosity, and decreases readability. As a universal library designed to satisfy a wide variety of DI topologies, this is the right trade-off for Dagger. Some applications require that flexibility and in those cases, Motif isn't suitable. Motif will be most effective in codebases that follow or adopt the following patterns:

* Granular scoping
* Deeply nested scopes
* Low intra-scope DI complexity

Below is a comparison between a Dagger and a Motif version of such an example.

#### Dagger ([Full Example](https://github.com/uber/motif/tree/master/samples/dagger-comparison/src/main/java/motif/daggercomparison/dagger))

```java
@RootComponent.Scope
@Component(modules = RootComponent.Module.class)
public interface RootComponent {

    RootController controller();

    LoggedInComponent.Builder loggedIn();

    @dagger.Component.Builder
    interface Builder {

        @BindsInstance
        Builder viewGroup(@Root ViewGroup parent);

        RootComponent build();
    }

    @dagger.Module
    abstract class Module {

        @Scope
        @Provides
        static RootView view(@Root ViewGroup parent) {
            return RootView.create(parent);
        }
    }

    @javax.inject.Scope
    @interface Scope {}

    @Qualifier
    @interface Root {}
}
```

Despite the simplicity of what we want to express, the above snippet touches on many concepts:

* @Scope
* @Component / @Subcomponent
* @Component.Buidler / @Subcomponent.Builder
* @Qualifier
* @BindsInstance
* @Module
* @Provides
* abstract @Modules
* static @Provides
* Component provision method
* Component factory method
* Constructor injection

Even for a comfortable Dagger user, it may take a few rounds of recompilation and deciphering of errors to get this code just right, and there is a continued tax associated with code readability. There are a number of different ways to achieve this same behavior, so developers should additionally understand why this pattern is preferred over others, introducing another layer of complexity. For example:

* @Subcomponents vs @Component.dependencies
* @BindsInstance vs Module constructor
* Scoped vs unscoped
* @Component.Builder vs generated API

#### Motif ([Full Example](https://github.com/uber/motif/tree/master/samples/dagger-comparison/src/main/java/motif/daggercomparison/motif))

```java
@Scope
public interface RootScope {

    RootController controller();

    LoggedInScope loggedIn(ViewGroup parentViewGroup);

    @motif.Objects
    abstract class Objects {

        abstract RootController controller();

        RootView view(ViewGroup viewGroup) {
            return RootView.create(viewGroup);
        }
    }
}
```

The Motif version is significantly shorter in terms of lines of code, but more importantly, it drastically reduces number of concepts a developer needs to understand. In fact, most of Motif's API is represented in this small example:

* @motif.Scope
* @motif.Objects
* Access method
* Child method
* Factory method (Basic)
* Factory method (Constructor Injected)

As an app scales up, the lightweight API encourages mitigating growing complexity by breaking down the DI graph into smaller scopes as opposed to supporting the requirements of larger scopes with more advanced DI library features.

Applications that commit to deep, granular DI graph hierarchies will see the most benefit from Motif. For codebases where this pattern isn't feasible everywhere or where incremental migration is preferred, Motif offers great [Dagger interoperability](https://github.com/uber/motif/blob/master/DAGGER.md). There will also be many situations in which Motif is just plain insufficient - and that's ok. Motif doesn't try to solve every use case, which is precisely how it's able to improve those cases it does target.

## Snapshots

Snapshots of the development version are available in [Sonatype's snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/com/uber/motif/).

## License

```
 Copyright (c) 2018 Uber Technologies, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
