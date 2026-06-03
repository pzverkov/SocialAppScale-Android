# SocialApp

A two-screen marketplace client, structured as a single Gradle module that splits into many without a rewrite.

**Status:** reference implementation. Single module, fully tested, release-signable with R8. Not wired to CI, and it carries a few deliberate dev-only settings documented under [Known gaps](#known-gaps-and-next-steps). Read "production-shaped," not "production-deployed."

## The architecture in one rule

Core knows nothing about features. Features never reach sideways.

`core` owns the infrastructure - networking, the persistence store, sharing, the navigation host, the design system - and depends on no feature. Each `feature/*` owns one product surface, layers itself `presentation -> domain -> data`, and reaches only into `core` or its own layers.

Most apps stay single-module until build times and ownership force a split, by which point dependencies point everywhere and the migration is expensive. Drawing the seam early costs nothing now and makes the later extraction mechanical: when a feature graduates to `:feature:itemlist`, its dependency edges already point the right way, so only the files move.

## Layout

```
app/src/main/java/com/pzverkov/socialapp
  SocialAppApplication.kt        # Hilt application entry point
  MainActivity.kt
  core/
    navigation/                  # SocialAppNavHost, typed routes, deep links
    network/                     # Retrofit/OkHttp setup, NetworkResult
    sharing/                     # ShareLinkBuilder, InstallationIdProvider
    store/                       # persistence primitives
    ui/                          # design system: theme, components, image loading
  feature/
    itemlist/
      data/                      # SocialAppApi, ItemRepositoryImpl, DTOs
      domain/                    # Item model, ItemRepository contract
      presentation/              # ItemListScreen, ViewModel, UI models
    itemdetail/
      presentation/              # ItemDetailScreen, ViewModel, UI models
    favorite/
      data/                      # SocialAppDatabase (Room), DAO, repository
      domain/                    # FavoriteRepository contract
```

The packages already name the module graph they will become:

```
:app
  :core:network   :core:ui   :core:navigation   :core:store   :core:sharing
  :feature:itemlist   :feature:itemdetail   :feature:favorite
```

Each `:feature:*` depends on the `:core:*` modules it uses and exposes its public surface through `domain`. `:app` assembles the graph through Hilt. No feature imports another, so the split untangles nothing.

## Stack

- Kotlin, JVM target 17, `compileSdk 36` / `minSdk 26` / `targetSdk 36`
- Jetpack Compose + Material 3, Compose Navigation for routing and deep links
- Hilt for dependency injection
- Retrofit + OkHttp + kotlinx.serialization for networking
- Room for favorites
- Coil for image loading
- JUnit, Turbine, Compose UI tests; Kover for coverage gates; R8 for release

## Engineering decisions

**Deep links resolve to content, not the home screen.** A custom scheme `socialapp://item/{id}` and verified App Links on `https://socialapp.app/item/{id}` both land on the item. Shared links carry an 8-character installation id (`?ref=`) so sharing is attributable without collecting PII. App Links need an `assetlinks.json` on the domain in production; the custom scheme works at install time.

**Hilt owns the object graph; tests swap bindings, not internals.** A view model never learns it is under test - the fake repository arrives through the binding the real one would.

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

A Node.js mock server under `mock-server/` serves the API locally:

```
cd mock-server && npm install && npm start
```

Exercise a deep link against a running build:

```
adb shell am start -a android.intent.action.VIEW -d "socialapp://item/1"
```

## Known gaps and next steps

Named on purpose, roughly in priority order:

- **Cleartext traffic is enabled** (`usesCleartextTraffic="true"`) to talk to the local mock server. Production needs this off and a `network-security-config` that pins or at least restricts to HTTPS.
- **No CI.** The Gradle tasks above are the contract a pipeline would run: assemble, unit tests, `koverVerify`, lint, instrumentation tests, signed release.
- **No crash reporting or observability.** `mapping.txt` retention is set up, but nothing consumes it yet. Wiring a reporter is a prerequisite for trusting a release.
- **The module split is designed, not performed.** The seams are real and the version catalog already shares versions across modules; carving `build.gradle` per module is the next commit.

---

For the full design rationale - the `Store` abstraction, error handling, testing philosophy, performance work, and the feature-onboarding playbook - see [ARCHITECTURE.md](ARCHITECTURE.md).
