# Motif

[![Build Status](https://travis-ci.org/uber/motif.svg?branch=master)](https://travis-ci.org/uber/motif)

*IMPORTANT: Motif is under heavy development. There will likely be breaking changes.*

Motif is a DI library that offers a simple API optimized for nested scopes. Under the hood it generates [Dagger](https://google.github.io/dagger/) code.

* [Gradle](#gradle)
* [Features](#features)
* [The Basics](#the-basics)
* [Child Scopes](#child-scopes)
* [Root Scopes](#root-scopes)
* [Convenience APIs](#convenience-apis)
* [Motif vs Dagger](#motif-vs-dagger)
* [Snapshots](#snapshots)
* [License](#license)

## Gradle

| [![Maven Central](https://img.shields.io/maven-central/v/com.uber.motif/motif-compiler.svg)](https://search.maven.org/artifact/com.uber.motif/motif-compiler)<br>[![Maven Central](https://img.shields.io/maven-central/v/com.uber.motif/motif.svg)](https://search.maven.org/artifact/com.uber.motif/motif) | <pre>annotationProcessor 'com.uber.motif:motif-compiler:x.y.z'<br>compile 'com.uber.motif:motif:x.y.z'</pre> |
|-|:-|

## Features

* [Minimal API](#the-basics)
* [IDE Integration](https://github.com/uber/motif/blob/master/plugin/README.md)
* [Dagger Interoperability](https://github.com/uber/motif/blob/master/DAGGER.md)

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

A class annotated with `@motif.Objects` class holds factory methods, which tell Motif how to create objects.

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

Object dependencies can be passed into factory methods as parameters as long as Motif knows how to instantiate them as well:

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

To retrieve objects from your Scope, define an access method on your Scope interface

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

Motif allows you to define a Scope as a child of another Scope by declaring a child method on the parent Scope interface:

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

Child Scopes can use objects provided by parent factory methods as long as they are annotated with `@Expose`:

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

        // No Database factory method.

        ChildView view() {
            return new ChildView();
        }

        ChildController controller(Database database, ChildView view) {
            return new ChildController(database, view);
        }
    }
}
```

You can create an instance of a child Scope by calling the parent's child method:

```java
MainScope mainScope = new MainScopeImpl();
ChildScope childScope = mainScope.child();
```

## Root Scopes

If Motif finds a nested interface annotated with `@Dependencies` on a Scope, it uses that interface to define exactly what this scope needs from its parent. This is required in order for Motif to tell you when you have unsatisfied dependencies. The recommended pattern is to always declare an empty `@Dependencies` interface on root Scopes:

```java
@motif.Scope
interface MainScope {

    // ...

    @motif.Dependencies
    interface Dependencies {}
}
```

With the root `Dependencies` interface in place, Motif will report any missing dependencies at build time. Without it, missing dependencies will still cause the build to fail, but the error messages will be less intuitive.

## Convenience APIs

Factory methods that pass parameters through to a constructor without modification can be converted to parameterless abstract methods:

<details>
<summary>Notes for Dagger users...</summary>

This feature is similar to Dagger's `@Inject` constructor injection, but it doesn't require annotating the class' constructor, and it scopes the object to the enclosing Motif Scope.
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

#### Dagger ([Full Example](https://github.com/uber/motif/tree/master/dagger-comparison/src/main/java/motif/daggercomparison/dagger))

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

#### Motif ([Full Example](https://github.com/uber/motif/tree/master/dagger-comparison/src/main/java/motif/daggercomparison/motif))

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
