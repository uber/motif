Our integration tests are written in a Java Gradle module `:it`. This is useful for leveraging all the niceties of the
IDE while writing the tests. However, we also need to be able to test cases where we expect Motif's annotation processor
 to throw an error. The `:it-compiler` module was built to support this case. It generates stub `*ScopeImpl` classes so
the IDE is happy when we're writing tests but doesn't do any correctness validation, so we are free test error cases as
well.