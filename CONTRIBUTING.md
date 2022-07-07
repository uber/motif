Contributing to Motif
=======================

The Uber team welcomes contributions of all kinds, from simple bug reports through documentation, test cases,
bugfixes, and features.

Workflow
--------

We love GitHub issues!

For small feature requests, an issue first proposing it for discussion or demo implementation in a PR suffice.

For big features, please open an issue so that we can agree on the direction, and hopefully avoid 
investing a lot of time on a feature that might need reworking.

Small pull requests for things like typos, bugfixes, etc are always welcome.

### Code style

This project uses [ktfmt](https://github.com/facebookincubator/ktfmt), [ktlint](https://github.com/pinterest/ktlint), and [GJF](https://github.com/google/google-java-format),
provided via the [spotless](https://github.com/diffplug/spotless) gradle plugin.

If you find that one of your pull reviews does not pass the CI server check due to a code style
conflict, you can easily fix it by running: `./gradlew spotlessApply`.

Generally speaking - we use ktfmt, vanilla ktlint + 2space indents, and vanilla GJF. You can integrate both of
these in IntelliJ code style via either [GJF's official plugin](https://plugins.jetbrains.com/plugin/8527-google-java-format) or applying code style from Jetbrains' official style.

No star imports please!

DOs and DON'Ts
--------------

* DO follow our [coding style](https://github.com/uber/java-code-styles)
* DO include tests when adding new features. When fixing bugs, start with adding a test that highlights how the current behavior is broken.
* DO keep the discussions focused. When a new or related topic comes up it's often better to create new issue than to side track the discussion.
* DO run all Gradle verification tasks (`./gradlew check`) before submitting a pull request

* DON'T submit PRs that alter licensing related files or headers. If you believe there's a problem with them, file an issue and we'll be happy to discuss it.

Building / Testing
------------------

The following will build and test all modules:

`./gradlew build`

Integration tests exist in the `:it` module. To run integration tests:

`./gradlew :compiler:test`
