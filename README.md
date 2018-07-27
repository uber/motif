# Motif

## The Basics

This is a Motif Scope. It doesn't do anything yet, but it will serve as a container for the objects you create:
<details>
<summary>Notes for Dagger users...</summary>
A Motif Scope is analogous to a Dagger `@Component`.
</details>

```java
import com.uber.motif.Scope;

@Scope
interface MainScope {}
```

A nested `Objects` class will hold all of your factory methods. Motif recognizes the special `Objects` class by its name:
<details>
<summary>Notes for Dagger users...</summary>
The nested `Objects` class is just like a Dagger `@Module` except Motif only allows you to define one `Objects` class per Scope.
</details>

```java
@Scope
interface MainScope {

    class Objects {}
}
```

To tell Motif how to create a `Controller` class, declare a factory method inside the `Objects` class:
<details>
<summary>Notes for Dagger users...</summary>
`Objects` factory methods are roughly the same as Dagger `@Provides` methods.
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

If your `Controller` needs a `View`, tell Motif how to create that as well, and pass the `View` in as a parameter to the controller factory method:
<details>
<summary>Notes for Dagger users...</summary>
Similar to Dagger `@Modules`, you can accept any type provided by a factory method as a parameter to other factory methods.
</details>

```java
@Scope
interface MainScope {

    class Objects {

        View view() {
            return new View();
        }

        Constroller controller(View view) {
            return new Controller(view);
        }
    }
}
```

Maybe your `Controller` needs a `Database` too. Declare it like you declared the `View` and pass it in as a second parameter to the `Controller` factory method:

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

If you compile the code above, Motif will generate a class in the same package called `MainScopeImpl` that implements your `MainScope` interface. Internally, the `MainScopeImpl` class knows all about the factory methods you've defined in your `Objects` class:
<details>
<summary>Notes for Dagger users...</summary>
The generated `*Impl` classes are similar to Dagger's generated `Dagger*` `@Component` implementation classes.
</details>

```java
MainScope mainScope = new MainScopeImpl();
```

But the `MainScope` interface is empty which is not very useful. To expose a way to instantiate your controller class, define a parameterless method with return type `Controller` on the top level `MainScope` interface:
<details>
<summary>Notes for Dagger users...</summary>
The `MainScope.controller()` method below is analogous to a Dagger `@Component` [provision method](https://google.github.io/dagger/api/2.14/dagger/Component.html#provision-methods).
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

Finally, instantiate a `Controller` by calling the `MainScope.controller` method you just defined. Under the hood, `MainScopeImpl` resolves the `Controller`'s dependencies and calls the appropriate factory methods automatically:

```java
MainScope mainScope = new MainScopeImpl();
Controller controller = mainScope.controller();
```

## Child Scopes

Most applications will benefit from organizing their dependencies into multiple Scopes. Let's define a new Scope called `ChildScope`:

```java
@Scope
interface ChildScope {}
```

Similarly to `MainScope`, your new `ChildScope` defines objects it can create on its nested `Objects` class and exposes a top-level `ChildScope.controller()` method to instantiate a `ChildController`:

```java
@Scope
interface ChildScope {

    ChildController controller();

    class Objects {

        ChildView view() {
            return new ChildView();
        }
        
        Database database() {
            return new Database();
        }

        ChildController controller(ChildView view, Database database) {
            return new ChildController(view, database);
        }
    }
}
```

And just like `MainScope` if you compile this code, Motif will generate a `ChildScopeImpl` class that implements the `ChildScope` interface, so it's possible to instantiate a `ChildScope` like this:

```java
ChildScope childScope = new ChildScopeImpl();
```

But instead of instantiating `ChildScopeImpl` directly, let's define an API that allows `MainScope` to create an instance of `ChildScope`. To do this, add a method to the `MainScope` interface with return type `ChildScope`:
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
In the following, we receive an instance of `ChildScope`. Behind the scenes, `MainScopeImpl` instantiates an instance of `ChildScopeImpl` - just as we would manually - and returns it via `mainScope.child()`. Motif knows to do this because it sees that the `ChildScope` class is annotated with `@Scope`:

```java
MainScope mainScope = new MainScopeImpl();
ChildScope childScope = mainScope.child();
```
To get an instance of a `MainController` and a `ChildController` you can write this:

```java
MainScope mainScope = new MainScopeImpl();
MainController mainController = mainScope.controller();

ChildScope childScope = mainScope.child();
ChildController childController = childScope.controller();
```

There's an issue here though. `MainScope` and `ChildScope` both define their own factory method for `Database` which means that the `MainController` and `ChildController` instances above each hold on to separate instances of `Database`. This is problematic if `Database` is meant to be a singleton or if instantiating a `Database` object is expensive, for example.

```java
MainScope mainScope = new MainScopeImpl();
MainController mainController = mainScope.controller();

ChildScope childScope = mainScope.child();
ChildController childController = childScope.controller();

assert(mainController.database == childController.database) // Fails!
```

To solve this, we can allow `ChildScope` to share `MainScope`'s `Database` instance instead of creating its own. This is possible because we've defined `ChildScope` as a child of `MainScope`. Remove the `Database` factory method in `ChildScope` to achieve this behavior:

```java
@Scope
interface ChildScope {

    ChildController controller();

    class Objects {

        ChildView view() {
            return new ChildView();
        }
        
        // Database factory method removed

        ChildController controller(ChildView view, Database database) {
            return new ChildController(view, database);
        }
    }
}
```

To allow `ChildScope` access to `MainScope`'s `Database` instance we also need to mark `MainScope`'s `Database` factory as public to indicate to Motif that the dependency should be visible to descendant Scopes:
<details>
<summary>Notes for Dagger users...</summary>
Motif considers depedencies private to the declaring Scope unless explicitly marked as `public`. This is different from Dagger's `@Subcomponents` which don't provide any built-in access control and expose all parent dependencies to descendant scopes by default.
</details>

```java
@Scope
interface MainScope {

    ChildScope child();

    // ...

    class Objects {

        // ...
        
        public Database database() {
            return new Database();
        }

        // ...
    }
}
```

Now, in the following code, `MainController` and `ChildController` share the same instance of `Database`:

```java
MainScope mainScope = new MainScopeImpl();
MainController mainController = mainScope.controller();

ChildScope childScope = mainScope.child();
ChildController childController = childScope.controller();

assert(mainController.database == childController.database) // Passes!
```


## Missing Dependencies



## Convenience APIs

In the examples above, the factory methods can actually be much more concise. Factory methods that simply pass parameters through to a constructor without modification can be converted to parameterless abstract methods. For example, you can update the `Objects.controller` method to the following:
<details>
<summary>Notes for Dagger users...</summary>
This feature is similar to Dagger's `@Inject` constructor injection, but it doesn't require annotating the class' constructor, and it scopes the object to the enclosing Motif Scope.
</details>

```java
@Scope
interface MainScope {

    Controller controller();

    abstract class Objects {

        View view() {
            return new View();
        }
        
        Database database() {
            return new Database();
        }

        abstract Constroller controller();
    }
}
```

The code above is equivalent to the previous version. Motif simply looks at the `Controller`'s constructor to determine its dependencies and how to instantiate it. You can do the same for `View` and `Database`:

```java
@Scope
interface MainScope {

    Controller controller();

    abstract class Objects {

        abstract View view();

        abstract Database database();

        abstract Constroller controller();
    }
}
```