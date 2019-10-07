# 0.2.1-SNAPSHOT

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
