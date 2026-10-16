# Releasing

## Release process

Creating and pushing a release tag to `uber/motif` automatically triggers the
[Release to Maven Central workflow](.github/workflows/release.yml). Merging a PR
or pushing a commit to `main` does not trigger a release.

The [release workflow](.github/workflows/release.yml) builds, signs, uploads, and
releases Motif's runtime and compiler libraries to public Maven Central through
the Sonatype Central Portal. The Gradle Maven Publish plugin's
`publishAndReleaseToMavenCentral` task handles both upload and release; no manual
promotion in Sonatype is required.

## Prepare and publish a release

1. Set `VERSION_NAME` in `gradle.properties` to the release version without
   `-SNAPSHOT`, for example `0.4.0-alpha16`.
2. Add a matching `# 0.4.0-alpha16` section to `CHANGELOG.md`.
3. Commit the changes with `Prepare for release 0.4.0-alpha16.` and merge the PR
   into `main`.
4. Tag the release commit and push that specific tag:

   ```sh
   git switch main
   git pull --ff-only origin main
   git tag -a 0.4.0-alpha16 -m "Version 0.4.0-alpha16"
   git push origin 0.4.0-alpha16
   ```

5. Follow **Release to Maven Central** in GitHub Actions. The workflow checks the
   libraries and runs their tests before publishing signed artifacts. Maven
   Central may take additional time to make a released version available.
6. Prepare the next development version, such as `0.4.0-alpha17-SNAPSHOT`, in
   `gradle.properties` and submit it in a separate PR with the commit message
   `Prepare next development version.`

Tags may use the existing format (`0.4.0-alpha16`) or an optional `v` prefix
(`v0.4.0-alpha16`). The tag version must exactly match the checked-out
`VERSION_NAME`, and snapshot versions are rejected. The workflow publishes the
tagged commit without changing versions, creating commits, or pushing tags.
It installs JDKs 8 and 11 for the library toolchains and runs Gradle on JDK 21.

## Manual run

For a manual run, select **Release to Maven Central** > **Run workflow** and enter
an existing release tag in `release_tag`. The workflow checks out that tag even
when launched from `main`. This can retry a failed release, but versions already
released to Maven Central cannot be overwritten; use a new version for changed
artifacts.

## Published modules

The workflow publishes `lib` (`motif`), `compiler` (`motif-compiler`),
`compiler-ksp` (`motif-compiler-ksp`), and their supporting modules: `ast`,
`compiler-ast`, `models`, `core`, and `errormessage`.

The workflow invokes publication tasks only for the modules listed above.
IntelliJ projects remain part of the Gradle build, but their publication tasks
are not invoked. IntelliJ artifacts will use a separate release workflow.

## Repository secrets

These GitHub Actions secrets are set at the Uber organization level and made
available to `uber/motif`. If required values are missing or any values are
incorrect, contact Uber GitHub Admin to fix the organization secrets or their
access for this repository.

| Secret | Value |
| --- | --- |
| `MAVEN_CENTRAL_USER_TOKEN_USERNAME` | Central Portal user token username with access to `com.uber.motif`. |
| `MAVEN_CENTRAL_USER_TOKEN_PASSWORD` | Central Portal user token password. |
| `PGP_SIGNING_KEY_PRIVATE_KEY` | ASCII-armored private signing key whose public key is available on a public key server. |
| `PGP_PRIVATE_KEY_ID` | Signing subkey ID, if required by the signing key. Optional. |
| `PGP_SIGNING_KEY_PASSWORD` | Private key passphrase. Optional for an unencrypted key. |
