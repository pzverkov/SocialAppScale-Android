# Contributing

This repository hosts two variants of the same app. Both are standalone Gradle builds that share one dependency catalog.

## Prerequisites

- Android SDK with `compileSdk 36`
- JDK 17 (the Gradle daemon pins and auto-provisions it; a matching `JAVA_HOME` is not required)

## Working on a variant

Each variant builds in isolation:

```
cd SocialApp-scale          # or SocialApp-basic
./gradlew :app:assembleDebug
```

## Quality gates

A change is ready when these pass in the variant you touched:

```
./gradlew testDebugUnitTest    # unit tests
./gradlew koverVerify          # coverage floor: 65% line, 60% branch
./gradlew lintDebug            # lint runs with abortOnError
```

Instrumentation tests need a device or emulator:

```
./gradlew connectedDebugAndroidTest
```

## Dependency versions

Plugin and library versions live in the shared catalog at `gradle/libs.versions.toml` and apply to both variants. Bump them there, then confirm both variants still build and pass the gates.

## Conventions

Each variant's `ARCHITECTURE.md` documents the design rules and the feature-onboarding playbook: state as a sealed interface, repository interfaces in `domain` and implementations in `data`, fakes over mocks, UI models separate from domain models. Match them.

## Commits and pull requests

- Write commit subjects in plain, lowercase, imperative form ("add favorites cache", not "Added favorites cache").
- Keep a change focused, and separate refactoring from behavior changes.
- Open a pull request once the quality gates pass, and describe what changed and why.
