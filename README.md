# Motif

## The Basics

This is a Motif Scope. It doesn't do anything yet, but it will serve as a container for the objects you create:
<details>
<summary>Notes for Dagger users...</summary>
A Motif Scope is analogous to a Dagger `@Component`.
</details>

```java
@motif.Scope
interface MainScope {}
```

A nested `Objects` class will hold all of your factory methods. Motif recognizes the special `Objects` class by its name:
<details>
<summary>Notes for Dagger users...</summary>
The nested `Objects` class is just like a Dagger `@Module` except Motif only allows you to define one `Objects` class per Scope.
</details>

```java
@motif.Scope
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
@motif.Scope
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
@motif.Scope
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

<pre><code class="java">@motif.Scope
interface MainScope {

    class Objects {

        View view() {
            return new View();
        }
        
        Database() {
            return new Database();
        }

        Constroller controller(View view, Database database) {
            return new Controller(view, database);
        }
    }
}
</code></pre>

If you compile the code above, Motif will generate a class in the same package called `MainScopeImpl` that implements your `MainScope` interface. Internally, the `MainScopeImpl` class knows all about the factory methods you've defined in your `Objects` class:
<details>
<summary>Notes for Dagger users...</summary>
The generated `*Impl` classes are similar to Dagger's generated `Dagger*` `Component` implementation classes.
</details>

```java
MainScope mainScope = new MainScopeImpl();
```

But the `MainScope` interface is empty which is not very useful. To expose a way to instantiate your controller class, define a parameterless method that returns `Controller` on the top level `MainScope` interface:
<details>
<summary>Notes for Dagger users...</summary>
The `controller()` method below is analogous to a Dagger `Component` [provision method](https://google.github.io/dagger/api/2.14/dagger/Component.html#provision-methods).
</details>

<pre><code class="java">@motif.Scope
interface MainScope {

    Controller controller();

    class Objects {

        View view() {
            return new View();
        }
        
        Database() {
            return new Database();
        }

        Constroller controller(View view, Database database) {
            return new Controller(view, database);
        }
    }
}
</code></pre>

Now we're able to instantiate our `Controller`. Under the hood, `MainScopeImpl` resolves the `Controller`'s dependencies and calls the appropriate factory methods automatically:

```java
MainScope mainScope = new MainScopeImpl();
Controller controller = mainScope.controller();
```

## Child Scopes

