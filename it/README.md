The tests in this module are run from the test harness in the ``:compiler` module. To run these tests, run
`./gradlew :compiler:test`

### TODO

#### Basic

- Naming collisions

#### Child Scopes

- Single child
- Dependency from parent
- Dynamic dependency
- Circular scope
- Circular scope: Cycle length > 2
- Increase visibility of base Objects class factory method
- Internal with qualifier

#### Dagger

- Dagger as parent
- Dagger as child
- Dagger w/ @Spread

#### Errors

- Circular dependencies
- Provided in multiple places (Not yet implemented)
- Missing dependency due to dependency visibility
- Missing dependency due to package-private method
- Missing dependency due to package-private spread method

- Invalid methods
- Non-interface annotated w/ @Scope