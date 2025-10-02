# 0.4.0-alpha09
* Fix race condition when skipping field initialization 
* 
# 0.4.0-alpha08
* Add support for skipping field initialization when initializing a scope.

# 0.4.0-alpha06
* Add support for Kotlin 2.1.0

# 0.4.0-alpha05
* Set JvmVersion to 1.8

# 0.4.0-alpha04
* Update Dagger version

# 0.4.0-alpha03
* Update XProcessing version

# 0.4.0-alpha02
* Upgrade JavaPoet, KotlinPoet, & XProcessing

# 0.4.0-alpha01
* Initial support for KSP

# 0.3.8

* Throw CannotResolveType error when compiler cannot resolve it.

# 0.3.7

* Fix edge case of compiler unable to find dependency methods with inner classes types.

# 0.3.6

* Fix edge case causing generating wrong method names.

# 0.3.5

* Update to Kotlin 1.5.10.
* IntelliJ scope navigation line marker now supports Kotlin files.

# 0.3.4

* Generated Kotlin code no longer results in compiler errors.

# 0.3.3

* Removed UnusedDependencyError. It's now valid to declare a dependency that is not consumed.

# 0.3.2

* Throw a compiler error when a Scope extends another Scope

# 0.3.1

* No changes

# 0.3.0

* Replace `@motif.Dependencies` API with `ScopeFactory.create`. See https://github.com/uber/motif/issues/125 for details.
* Do not use computeIfAbsent in ScopeFactory in order to avoid ConcurrentModificationException in the IntelliJ plugin
* Allow graph processing to continue even when a ParsingError is encountered. A malformed Scope will simply not be included in the ResolvedGraph.
* New `ResolvedGraph.getParentEdges(Scope)` and `ResolvedGraph.getScope(IrType)` APIs

# 0.2.1

* Improve error message when generate name for ErrorTypes
* Improve invalid Scope method error messages
* Remove option to generate a Dagger-based implementation
* Fix motif allowing multiple parameters of same type for a child method
* Only allow memberless qualifiers or qualifiers with a single String `value` member

# 0.2.0

* `-Amotif.mode=[dagger|java|kotlin]` for different [code generation modes](https://github.com/uber/motif/wiki#code-generation-mode)
* -Anodagger no longer has any effect

# 0.1.3

* Add Javadocs mentioning original consumers of generated Dependencies methods

# 0.1.2

* Enable Daggerless code generation with `-Anodagger=true`

# 0.1.1

* Disallow @Nullable child method parameters

# 0.1.0
