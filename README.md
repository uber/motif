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

```java
@motif.Scope
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
```

