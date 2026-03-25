# T079 Observer Test

This test verifies that the Motif Observer feature works correctly.

## Feature Description

When a `@Scope` annotation has `enableObserver = true`, the generated `ScopeImpl` will:

1. Call `MotifObserver.notifyScopeInitializing(scopeClassName)` in the constructor
2. Wrap all factory/provider methods with:
   - `MotifObserver.notifyProvideStart(scopeClassName, methodName)` before execution
   - `MotifObserver.notifyProvideComplete(scopeClassName, methodName)` after execution (in finally block)

## Test Verification

The `Test.java` file:
1. Registers a `TestObserver` that tracks all events
2. Creates a `ScopeImpl` instance and verifies `onScopeInitializing` was called
3. Calls `scope.string()` and `scope.number()` and verifies `onProvideStart` and `onProvideComplete` events
4. Tests unregistering an observer and verifies events stop being tracked
5. Cleans up all observers

## Expected Events Sequence

1. `onScopeInitializing: testcases.T079_observer.Scope`
2. `onProvideStart: testcases.T079_observer.Scope - string`
3. `onProvideComplete: testcases.T079_observer.Scope - string`
4. `onProvideStart: testcases.T079_observer.Scope - number`
5. `onProvideComplete: testcases.T079_observer.Scope - number`

## Implementation Files

- `/lib/src/main/java/motif/observe/Observer.java` - Observer interface
- `/lib/src/main/java/motif/observe/MotifObserver.java` - Observer registry and notification dispatcher
- `/lib/src/main/java/motif/Scope.java` - Updated with `enableObserver` parameter
- Code generators updated to emit observer calls in generated `ScopeImpl` classes
