# Motif

## The Basics

This is a Motif Scope. It serves as a container for objects that can be created by this Scope:

<details>
<summary>Notes for Dagger users...</summary>

A Motif Scope is analogous to a Dagger `@Component`.
</details>

```java
import motif.Scope;

@Scope
interface MainScope {}
```

A specially named `Objects` class holds factory methods, which tell Motif how to create objects.

<details>
<summary>Notes for Dagger users...</summary>

The nested `Objects` class is just like a Dagger `@Module` except Motif only allows you to define one `Objects` class per Scope. Factory methods are analogous to `@Provides` methods.
</details>

```java
@Scope
interface MainScope {

    class Objects {

        Constroller controller() {
            return new Controller();
        }
    }
}
```

Object dependencies can be passed into factory methods as parameters as long as Motif knows how to instantiate them as well:

```java
@Scope
interface MainScope {

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
@Scope
interface MainScope {

    Controller controller();

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
@Scope
interface MainScope {

    ChildScope child();
    
    // ...
}
```

Child Scopes can use objects declared on parent's `Objects` classes as long as they are marked public:

<details>
<summary>Notes for Dagger users...</summary>

Unlike Dagger `@Subcomponents` which expose all objects down the graph by default, Motif Scopes consider objects internal to the Scope unless explicitly marked as public.
</details>

```java
@Scope
interface MainScope {

    ChildScope child();

    // ...

    class Objects {

        public Database database() {
            return new Database();
        }

        // ...
    }
}

@Scope
interface ChildScope {

    ChildController controller();

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

If Motif finds a nested interface named `Parent` on a Scope, it uses that interface to define exactly what is passed from the parent to the child. This is required in order for Motif to tell you when you have unsatisfied dependencies. The recommended pattern is to always declare an empty `Parent` interface on root Scopes:

```java
@Scope
interface MainScope {

    // ...
    
    interface Parent {}
}
```

With the root `Parent` interface in place, Motif will report any missing dependencies at build time. Without it, missing dependencies will still cause the build to fail, but the error messages will be less intuitive.

## Convenience APIs

Factory methods that pass parameters through to a constructor without modification can be converted to parameterless abstract methods:

<details>
<summary>Notes for Dagger users...</summary>

This feature is similar to Dagger's `@Inject` constructor injection, but it doesn't require annotating the class' constructor, and it scopes the object to the enclosing Motif Scope.
</details>

```java
@Scope
interface MainScope {

    // ...

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

@Scope
interface MainScope {

    // ...

    abstract class Objects implements ControllerObjects<Controller, View> {
        abstract Database database();
    }
}
```