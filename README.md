# Motif

## The Basics

This is a Motif Scope. It serves as a container for objects that can be created by this Scope:
<details>
<summary>Notes for Dagger users...</summary>
A Motif Scope is analogous to a Dagger `@Component`.
</details>

```java
import com.uber.motif.Scope;

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

Finally, Motif generates an implementation class for each scope, allowing access to the Scope's objects:

```java
MainScope mainScope = new MainScopeImpl();
Controller controller = mainScope.controller();
```

## Child Scopes

Motif allows you to define a Scope as a child of another Scope:
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

With the above Scope definitions, the following passes:

```java
MainScope mainScope = new MainScopeImpl();
MainController mainController = mainScope.controller();

ChildScope childScope = mainScope.child();
ChildController childController = childScope.controller();

assert(mainController.database == childController.database)
```

## Missing Dependencies

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