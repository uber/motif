# Releasing

## Release process

Creating and pushing a release tag to `uber/motif` automatically triggers the
[Release to Maven Central workflow](.github/workflows/release.yml). Merging a PR
or pushing a commit to `main` does not trigger a release.

The [release workflow](.github/workflows/release.yml) builds, signs, uploads, and
releases Motif's Maven libraries to public Maven Central through the Sonatype
Central Portal. The Gradle Maven Publish plugin's `publishToMavenCentral` task
handles both upload and release because `mavenCentralAutomaticPublishing=true`
is set in `gradle.properties`; no manual promotion in Sonatype is required.
After publishing, the workflow creates a GitHub Release for the tag using the
matching `CHANGELOG.md` section as its release notes. Versions with a prerelease
suffix, such as `0.4.0-alpha16`, are marked as prereleases on GitHub. The tagged
commit should already have passed CI on `main`.

## Prepare and publish a release

1. Set `VERSION_NAME` in `gradle.properties` to the release version without
   `-SNAPSHOT`, for example `0.4.0-alpha16`.
2. Add a matching `# 0.4.0-alpha16` section with release notes to `CHANGELOG.md`.
3. Commit the changes with `Prepare for release 0.4.0-alpha16.` and merge the PR
   into `main`.
4. Tag the release commit and push that specific tag:

   ```sh
   git switch main
   git pull --ff-only origin main
   git tag -a 0.4.0-alpha16 -m "Version 0.4.0-alpha16"
   git push origin 0.4.0-alpha16
   ```

5. Follow **Release to Maven Central** in GitHub Actions. The workflow validates
   the tag, version, and changelog section before publishing signed artifacts,
   then creates the GitHub Release. Maven Central may take additional time to
   make a released version available.
6. Prepare the next development version, such as `0.4.0-alpha17-SNAPSHOT`, in
   `gradle.properties` and submit it in a separate PR with the commit message
   `Prepare next development version.`

Tags may use the existing format (`0.4.0-alpha16`) or an optional `v` prefix
(`v0.4.0-alpha16`). The tag version must exactly match the checked-out
`VERSION_NAME`, and snapshot versions are rejected. The workflow publishes the
tagged commit without changing versions, creating commits, or pushing tags.
It installs JDKs 8 and 11 for the library toolchains and runs Gradle on JDK 21.

## Retry a release

Open the failed tag-push run of **Release to Maven Central** in GitHub Actions
and select **Re-run failed jobs**. Versions already released to Maven Central
cannot be overwritten; use a new version for changed artifacts.

## Published modules

The workflow publishes `lib` (`motif`), `compiler` (`motif-compiler`),
`compiler-ksp` (`motif-compiler-ksp`), and their supporting modules: `ast`,
`compiler-ast`, `models`, `core`, and `errormessage`. It also continues publishing
`viewmodel` (`motif-viewmodel`), `xprocessing` (`motif-xprocessing`), and
`xprocessing-testing` (`motif-xprocessing-testing`).

The root `publishToMavenCentral` task also includes the two IntelliJ Maven
artifacts, for thirteen modules in total:

| Gradle project | Maven artifact |
| --- | --- |
| `intellij` | `motif-intellij` |
| `intellij-ast` | `motif-ast-intellij` |

Both Maven artifacts use `VERSION_NAME`, matching the other libraries. The
`intellij` project's plugin version (`0.0.5`) is separate from its Maven
publication version.

Publishing the plugin to JetBrains Marketplace uses `publishPlugin`, which is
not invoked by this Maven workflow. Marketplace publishing will use a separate
release workflow.

## Snapshots

CI publishes snapshots from successful pushes to `main` when `VERSION_NAME`
ends in `-SNAPSHOT`. Pushes with a release version skip snapshot publication.

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
| `PGP_PRIVATE_KEY_ID` | Signing subkey ID. |
| `PGP_SIGNING_KEY_PASSWORD` | Private key passphrase. Optional for an unencrypted key. |
