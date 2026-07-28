Releasing
=========

 1. Change the version in `gradle.properties` to a non-SNAPSHOT version.
 2. Update the `CHANGELOG.md` for the impending release.
 3. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
 4. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
 5. `git push && git push --tags`
 6. `CI=true ./gradlew clean publish -Dorg.gradle.parallel=false`
 7. Update the `gradle.properties` to the next SNAPSHOT version.
 8. `git commit -am "Prepare next development version."`
 9. `git push`
 10. Visit [Sonatype Central](https://central.sonatype.com/) and promote the artifact.
     - Select the artifact, click `close`, wait for it to close, then select again and click
     `release`.
