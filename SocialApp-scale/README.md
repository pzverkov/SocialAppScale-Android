# SocialApp

A two-screen marketplace client, split into Gradle modules behind convention plugins, with dependency injection by Metro.

**Status:** reference implementation. Multi-module (`:core:*` plus a `:feature:*` module per feature), fully tested, release-signable with R8, and verified in CI. It carries a few deliberate dev-only settings documented under [Known gaps](#known-gaps-and-next-steps). Read "production-shaped," not "production-deployed."

## The architecture in one rule

Core knows nothing about features. Features never reach sideways.

`core` owns the infrastructure - networking, the persistence store, sharing, the navigation host, the design system - and depends on no feature. Each `feature/*` owns one product surface, layers itself `presentation -> domain -> data`, and reaches only into `core` or its own layers.

Most apps stay single-module until build times and ownership force a split, by which point dependencies point everywhere and the migration is expensive. The seam was drawn early, so extraction was mechanical: the `:core:*` modules and every feature are their own Gradle projects, and `:app` is left as a thin composition root - files relocated, edges already pointed right.

## Layout

```
SocialApp-scale/
  build-logic/                   # convention plugins: socialapp.android.*, socialapp.jvm.library
  core/
    model/         (:core:model)    # Item, ErrorType - pure Kotlin
    domain/        (:core:domain)   # repository + CrashReporter contracts, NetworkResult - pure Kotlin
    common/        (:core:common)   # Store + StoreInterceptor, PriceFormatter - pure Kotlin
    network/       (:core:network)  # Retrofit/OkHttp setup, per-build-type BASE_URL
    ui/            (:core:ui)       # design system: theme, components, image loading
    sharing/       (:core:sharing)  # ShareLinkBuilder, InstallationIdProvider
    navigation/    (:core:navigation)     # shared deeplink scheme/host constants - pure Kotlin
    observability/ (:core:observability)  # CrashReporter impl + Store breadcrumb interceptor
    ai/            (:core:ai)       # on-device AI: Gemini Nano (GenAI) + ML Kit translation
    testing/       (:core:testing)  # shared test doubles (fakes for the contracts)
  feature/
    itemlist/   (:feature:itemlist)    # SocialAppApi, repo, DTOs, list screen + ViewModel, nav contract
    itemdetail/ (:feature:itemdetail)  # detail screen + ViewModel, AI summary, translation, alt text, nav contract
    favorite/   (:feature:favorite)    # data only (Room), no UI
  app/                           # Application, MainActivity, DI graph, navigation host
    core/di, core/navigation     # graph aggregation + NavHost stay in :app
```

Every feature is its own module behind the `socialapp.android.feature` convention (favorite, being data-only, uses the plain library convention). `:app` is a thin composition root - it depends on each feature to aggregate their `@Contributes*` declarations and nav contracts, but holds no feature code:

```
:app                ->  :feature:itemlist  :feature:itemdetail  :feature:favorite  :core:observability  :core:ai
:feature:itemlist   ->  :core:model  :core:domain  :core:common  :core:network  :core:ui  :core:sharing
:feature:itemdetail ->  :core:model  :core:domain  :core:common  :core:ui  :core:sharing  :core:navigation  :core:ai
:feature:favorite   ->  :core:domain
```

A build-logic rule (`socialapp.module.rules`) fails the build if a feature depends on another feature or a core module depends on a feature.

Repository contracts live in `:core:domain`, so a feature's view model reads another feature's data through the shared interface without importing that feature. `:app` assembles the Metro graph from `@Contributes*` declarations across modules. No feature imports another, so the split untangles nothing.

## Stack

- Kotlin, JVM target 17, `compileSdk 36` / `minSdk 26` / `targetSdk 36`
- Jetpack Compose + Material 3, Compose Navigation for routing and deep links
- Metro for dependency injection: compile-time graph, per-module `@Contributes*`
- Retrofit + OkHttp + kotlinx.serialization for networking
- Room for favorites
- Coil for image loading
- JUnit, Turbine, Compose UI tests; Kover for coverage gates; R8 for release

## Engineering decisions

**Deep links resolve to content, not the home screen.** A custom scheme `socialapp://item/{id}` and verified App Links on `https://socialapp.app/item/{id}` both land on the item. Shared links carry an 8-character installation id (`?ref=`) so sharing is attributable without collecting PII. App Links need an `assetlinks.json` on the domain in production; the custom scheme works at install time.

**Metro owns the object graph; tests swap bindings, not internals.** Modules contribute bindings with `@ContributesBinding`, and instrumentation replaces one with `replaces = [...]`. A view model never learns it is under test - the fake repository arrives through the binding the real one would.

**Coverage is a floor the build enforces.** Kover fails below 65% line and 60% branch. Generated and UI-only code is excluded, so the number measures logic rather than Compose boilerplate. There are no screenshot or end-to-end tests yet.

**Accessibility is treated as acceptance criteria.** 48dp touch targets, WCAG contrast ratios, live regions on state changes, and errors typed and mapped to localized strings.

**Lint fails the build.** `abortOnError` is on; known issues live in a checked-in baseline so they stay visible rather than suppressed inline.

**Release is minified and signed from credentials that never touch git.** R8 shrinks code and resources. Keep rules cover only what R8 cannot infer (kotlinx.serialization serializers); the rest ship consumer rules. Signing reads `keystore.properties` or CI environment variables; miss both and the build produces an unsigned APK rather than one signed with the debug key. R8 emits a `mapping.txt` per release build - retain it per version to deobfuscate production stack traces.

**The build is reproducible across machines.** Every plugin and dependency version lives in one Gradle version catalog (`gradle/libs.versions.toml`), and the Gradle daemon is pinned to a JDK 17 toolchain (`gradle/gradle-daemon-jvm.properties`, auto-provisioned via the foojay resolver), so CLI, IDE, and CI compile on the same JVM instead of whatever happens to launch Gradle.

## Build, test, release

```
./gradlew :app:assembleDebug        # debug APK
./gradlew test                      # JVM unit tests
./gradlew koverHtmlReport           # coverage report
./gradlew koverVerify               # enforce the coverage floor
./gradlew connectedAndroidTest      # instrumentation tests (device/emulator)
./gradlew :app:assembleRelease      # minified, signed release APK
```

Release signing pulls from a gitignored `keystore.properties`. Set it up once:

```
keytool -genkeypair -v -keystore app/release.jks -alias socialapp \
  -keyalg RSA -keysize 2048 -validity 10000
cp keystore.properties.template keystore.properties   # then fill in the passwords
```

CI can skip the file and export `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` instead. The release key is the app's identity for its lifetime; in a real deployment it belongs in a secret manager or hardware-backed store, not on a developer machine.

A dependency-free Node.js mock server under `mock-server/` serves the API locally (nothing to install):

```
cd mock-server && node server.js
```

Exercise a deep link against a running build:

```
adb shell am start -a android.intent.action.VIEW -d "socialapp://item/1"
```

## Known gaps and next steps

Named on purpose, roughly in priority order:

- **No real backend.** Cleartext is now off on the release path (a `network-security-config` forbids it; a debug-only overlay permits it solely for the `10.0.2.2`/`localhost` mock server) and `BASE_URL` is a per-build-type field. The release host is a placeholder - production needs a real HTTPS API, plus the hosted `assetlinks.json` for App Links and ideally certificate pinning.
- **Observability has a seam, not a backend.** A `CrashReporter` contract with a Logcat binding and a Store `BreadcrumbInterceptor` are wired; swapping in a vendor SDK (Crashlytics, Sentry) and uploading `mapping.txt` per release from CI is the remaining step.
- **Some on-device AI needs capable hardware.** Summarization and image description run on Gemini Nano (ML Kit GenAI), present only on flagship devices (Pixel 9/10, Galaxy S25/S26 class); elsewhere the client reports unavailable and those affordances stay hidden. Translation uses classic ML Kit and runs on nearly all devices, downloading a language model on first use. The app is fully functional without any of them.

---

For the full design rationale - the `Store` abstraction, error handling, testing philosophy, performance work, and the feature-onboarding playbook - see [ARCHITECTURE.md](ARCHITECTURE.md).
