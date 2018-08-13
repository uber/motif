# Motif

Motif is a DI library that offers a simple API optimized for nested scopes. Under the hood it generates [Dagger](https://google.github.io/dagger/) code.

*IMPORTANT: Motif is under heavy development. There will likely be breaking changes.*

## Features

* [Minimal API](#the-basics)
* [IDE Integration](https://github.com/uber/motif/blob/master/plugin/README.md)
* [Interoperability with Dagger Components](https://github.com/uber/motif/blob/master/DAGGER.md)

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

        Constroller controller(View view, Database database) {
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

        Constroller controller(View view, Database database) {
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
        abstract Constroller controller();
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