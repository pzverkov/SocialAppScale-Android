# SocialApp

[![SocialApp-basic](https://github.com/pzverkov/SocialAppScale-Android/actions/workflows/ci-basic.yml/badge.svg)](https://github.com/pzverkov/SocialAppScale-Android/actions/workflows/ci-basic.yml)
[![SocialApp-scale](https://github.com/pzverkov/SocialAppScale-Android/actions/workflows/ci-scale.yml/badge.svg)](https://github.com/pzverkov/SocialAppScale-Android/actions/workflows/ci-scale.yml)

A two-screen Android marketplace app, kept as two variants in one repository.

| Variant | Role |
| --- | --- |
| [`SocialApp-basic`](SocialApp-basic/) | Lean single-module reference - the smallest build that does the job. |
| [`SocialApp-scale`](SocialApp-scale/) | Production-shaped variant, structured for the multi-module split. |

Both variants build from the same baseline today. `SocialApp-scale` is the track for production hardening and the `:core:*` / `:feature:*` module split; `SocialApp-basic` stays the lean reference. Each variant carries its own `README.md` and `ARCHITECTURE.md` with the full detail.

## Build

Each variant is a standalone Gradle build. Pick one and run its wrapper:

```
cd SocialApp-scale          # or SocialApp-basic
./gradlew :app:assembleDebug
```

Requirements: an Android SDK (`compileSdk 36`) and JDK 17. The Gradle daemon is pinned to a JDK 17 toolchain and auto-provisions it via the foojay resolver, so a matching `JAVA_HOME` is not required.

The build cache, configuration cache, and parallel execution are on by default (`gradle.properties`), so repeat builds reuse task outputs and rebuild only what changed.

A local mock API serves the item data:

```
cd SocialApp-scale && node mock-server/server.js   # emulator reaches it at http://10.0.2.2:3000
```

## Layout

```
SocialApp/
  gradle/libs.versions.toml   # shared dependency and plugin versions for both variants
  SocialApp-basic/            # standalone Gradle build
  SocialApp-scale/            # standalone Gradle build
```

Plugin and dependency versions live in the single shared catalog at `gradle/libs.versions.toml`; both variants import it, so a bump applies to both at once. Everything else (wrapper, build scripts, app code) is per-variant.

## More

- Building, testing, and quality gates: [CONTRIBUTING.md](CONTRIBUTING.md)
- Security policy: [SECURITY.md](SECURITY.md)
- License: [MIT](LICENSE)
