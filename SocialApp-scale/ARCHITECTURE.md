# Architecture

Design rationale and conventions for SocialApp. For what the app is and how to build it, see the [README](README.md).

## Foundations

Five choices set the shape of the app, each stated with the alternative it beats:

1. **Item identity flows through Navigation arguments, not shared state.** The detail screen takes an item ID and loads from the repository, rather than reading a global object the list screen wrote. A shared singleton breaks on process death, resists testing, and couples the two screens implicitly.
2. **Coil for image loading.** Kotlin-first, with built-in Compose support (`AsyncImage`), memory and disk caching, and cancellation when a view detaches. Over Glide, a smaller API surface and first-class Compose integration; over a raw `Thread`, none of which has caching, cancellation, or leak-safety.
3. **Compose `LazyVerticalGrid` for the list.** Recycling, diffing, and layout in one API, instead of a hand-rolled `BaseAdapter` that re-inflates and `findViewById`s on every bind.
4. **ViewModels, not presenters.** State survives configuration changes and the ViewModel holds no view reference, removing the manual attach/detach lifecycle dance and the indirection of a `BaseFragment<V, P>` hierarchy.
5. **Tests with a coverage gate.** Unit and instrumentation tests, with Kover enforcing a floor in the build.

## Approach

Jetpack Compose with a unidirectional-data-flow architecture. Single Activity (no Fragment lifecycle to manage; navigation is declarative via Compose Navigation), no XML layouts. The shape optimizes for one thing: adding a third screen is mechanical - create a ViewModel, define the state, write the composable, register the route.

Core features: debounced search (300ms, the standard threshold where typing feels instant but filtering doesn't fire on every keystroke), favorites with Room, and deep links with share attribution. Deliberately out of scope: pagination (12 items, no pagination API), offline caching (the mock server is localhost), and a Compose preview for every state (three previews demonstrate the pattern without a gallery).

## Data flow and key decisions

```
Composable Screen
    |
    | collects StateFlow<State> + SharedFlow<Event>
    v
ViewModel + Store<S, E>
    |
    | calls Repository interfaces directly
    v
Repository (data layer)
    |
    | Retrofit (remote) / Room (local)
    v
Data sources
```

**Why UDF over MVP:** MVP requires manual view attachment/detachment, nullable view references, and explicit lifecycle management. UDF with StateFlow eliminates all three: state flows down as an immutable sealed interface, the ViewModel doesn't hold a reference to the view, and Compose handles lifecycle collection via `collectAsStateWithLifecycle`.

**Metro over Hilt for the modular variant:** Hilt cannot process annotations in feature modules (Google's own guidance is to drop to Dagger there), the exact wall a modular app hits. Metro is a compiler-plugin DI with compile-time graph validation and first-party per-module contributions (`@ContributesBinding`, `@ContributesIntoMap`): each module declares its own bindings and the app graph aggregates them across the build. The single-module `SocialApp-basic` stays on Hilt as the deliberate contrast - batteries-included DI is the right call until modularization makes its limits bite. ViewModels resolve through `metrox-viewmodel-compose`; the detail screen uses assisted injection for its `itemId`.

**kotlinx.serialization over Gson:** Kotlin-native, no reflection at runtime, compile-time safety via `@Serializable`. Gson uses reflection which is slower and can't catch schema mismatches until runtime.

**Sealed interfaces over sealed classes:** Less bytecode, allows implementing multiple sealed hierarchies if needed. The `when` expression is exhaustive for both, but interfaces are the modern Kotlin idiom.

**Why no UseCases:** ViewModels call repository interfaces directly. A UseCase that does `suspend fun invoke() = repository.getItems()` adds a file, a class, and an injection point for zero behavior. A UseCase earns its place only with real logic (validation, orchestrating multiple repositories, transforming data); there are none here, rather than empty pass-throughs that demonstrate a pattern without using it.

## Store Abstraction

The `Store<State, Event>` wraps `MutableStateFlow` for screen state and `MutableSharedFlow` for one-shot events.

**Why not use raw flows directly?** The SharedFlow configuration is non-obvious and error-prone if repeated: `extraBufferCapacity = 1` with `DROP_OLDEST` overflow and `tryEmit` (non-suspending) ensures events aren't lost during brief recomposition gaps and avoids coroutine leaks from suspended `emit` calls without collectors. The `updateState` function takes a reducer `(S) -> S`, using `MutableStateFlow.update` which retries on concurrent modification. In this codebase all state updates happen on `Dispatchers.Main`, so the retry mechanism is defensive rather than load-bearing, but it's the correct API to use regardless.

The Store is the point where interceptors plug in (logging, analytics, test state recording) without modifying each ViewModel. A `StoreInterceptor` observes every state transition and event; ViewModels collect a `Set<StoreInterceptor>` Metro multibinding and pass it to their Store. The `:core:observability` module contributes a `BreadcrumbInterceptor` that feeds those transitions to a `CrashReporter` (Logcat today, swappable for a vendor SDK through the DI binding), so a crash report carries the trail of UI states that led to it.

## Module Structure

The `:core:*` layers and every `:feature:*` are Gradle modules behind convention plugins. `:app` is a thin composition root: it owns the Metro graph, the `NavHost`, `MainActivity`, and the `Application`, and depends on each feature to aggregate their contributions, but contains no feature screen, ViewModel, or data code.

```
:core:model         # Item, ErrorType (pure Kotlin, no deps)
:core:domain        # ItemRepository, FavoriteRepository, CrashReporter, NetworkResult -> :core:model
:core:common        # Store<State, Event> + StoreInterceptor, PriceFormatter (pure Kotlin)
:core:network       # OkHttp, Retrofit wiring, per-build-type BASE_URL
:core:ui            # components, theme, image loading -> :core:model
:core:sharing       # InstallationIdProvider, ShareLinkBuilder
:core:navigation    # shared deeplink scheme/host constants (pure Kotlin)
:core:observability # LogcatCrashReporter + BreadcrumbInterceptor -> :core:common, :core:domain
:core:ai            # OnDeviceAiClient: on-device Gemini Nano via ML Kit GenAI, device-gated
:core:testing       # shared test doubles (fakes for the domain contracts) -> :core:domain

:feature:itemlist    # SocialAppApi, ItemRepositoryImpl, DTO, list screen + ViewModel, nav contract
                     # -> :core:{model,domain,common,network,ui,sharing}
:feature:itemdetail  # detail screen + assisted-injected ViewModel, nav contract
                     # -> :core:{model,domain,common,ui,sharing,navigation}
:feature:favorite    # data only (Room DB, DAO, entity), pure library + Metro, no UI
                     # -> :core:domain

:app
└── com.pzverkov.socialapp/
    ├── core/di/           # Metro AppGraph, ViewModel factory
    ├── core/navigation/   # NavHost composing the per-feature nav contracts
    ├── MainActivity.kt
    └── SocialAppApplication.kt
```

`SocialAppApi` lives in `:feature:itemlist`, not in `:core:network`. Core provides infrastructure (the Retrofit instance, the OkHttp client); features own their API interfaces. The repository contracts sit in `:core:domain`, so itemdetail reaches item data through the interface, not through `:feature:itemlist`. No feature imports another; a build-logic rule (`socialapp.module.rules`) fails configuration if one does, or if a core module depends on a feature.

**Navigation contract.** Each screen-bearing feature exposes a type-safe `@Serializable` route, a `NavController.navigateToX()` helper, and a `NavGraphBuilder.xScreen(...)` extension that registers its composable and deeplinks. `:app`'s `NavHost` calls those extensions and wires the lambdas; it imports no screen composable and owns no route strings. Deeplinks stay as explicit URI patterns (built from the `:core:navigation` scheme/host constants) so they match the manifest intent filters exactly, while `toRoute()` rebuilds the route from the parsed arguments.

## Error Handling

The repository catches exceptions and returns a typed `ErrorType` enum (`NETWORK`, `UNKNOWN`), not raw exception messages. The presentation layer maps `ErrorType` to localized strings via `strings.xml`. This keeps the data layer free of UI concerns and enables localization.

`CancellationException` is explicitly rethrown. The generic `catch (e: Exception)` in Kotlin coroutines swallows `CancellationException`, which breaks structured concurrency. Every `try/catch` in the repository handles this.

## Testing Strategy

**Philosophy:** Fakes, not mocks. Every test creates a fake implementation of the repository interface with controllable return values. This tests behavior ("given these items, does the ViewModel produce the correct state sequence?") rather than interactions ("did you call `getItems()` once?"). Fakes survive refactoring because they don't couple tests to internal method call sequences.

**Unit tests:** ViewModel state transitions (loading, loaded, error, empty, search, favorites, retry, grid toggle, share). Repository caching, force refresh, error types, CancellationException propagation. Store state updates and event buffering. DTO deserialization with hardcoded JSON matching the mock server format. ShareLinkBuilder URL format.

**Instrumentation tests:** Full integration via a Metro test graph that contributes a fake repository with `replaces = [...]`. Item rendering, search interaction, favorite toggle, navigation, buy snackbar, deeplinks, back navigation.

**Coverage:** Kover with 65% line and 60% branch minimums. Why 65% and not higher: the codebase is Compose-heavy, and composable functions are excluded from unit test coverage (they're covered by instrumentation tests). 65% ensures business logic, repositories, ViewModels, and the Store are thoroughly tested without gaming the number with trivial UI assertions.

## Accessibility

For a marketplace with a diverse, international user base, accessibility is both a product and a legal requirement - for example the EU's European Accessibility Act (Directive 2019/882), enforceable since June 2025.

**Implemented:**
- 48dp minimum touch targets on all interactive elements (WCAG 2.5.8, exceeding the 44dp minimum)
- Dark scrim overlays (0.65+ alpha) on image-backed text to maximize contrast. WCAG 1.4.3 cannot be statically verified on image overlays since the final contrast depends on image content, but the scrim provides a consistent floor
- `LiveRegion` semantics on state-change areas for screen reader announcements (WCAG 4.1.3)
- `heading()` semantics on section titles for screen reader navigation (WCAG 1.3.1)
- All strings in `strings.xml` for localization readiness
- Dark mode via Material You on Android 12+, fallback dark scheme on older devices
- Content descriptions on all interactive icons from string resources
- Clickable location that opens the device's maps app via `geo:` intent

**Not yet covered:** Automated accessibility scanning in CI, TalkBack end-to-end testing, RTL layout verification, font scaling stress testing.

## On-device AI

On-device AI lives in `:core:ai`, split across two backends with different reach. `OnDeviceAiClient` wraps Gemini Nano via ML Kit GenAI (AICore) for description summarization and image description; `OnDeviceTranslator` wraps classic ML Kit translation + language identification for translating an item's description. The two are separate interfaces because their availability models differ: Gemini Nano is flagship-gated, while translation runs on essentially any device and only pays a one-time per-language-pair model download.

**Capability gating is the contract, not an afterthought.** ML Kit GenAI runs only on specific flagship hardware (Pixel 9/10, Galaxy S25/S26 class). Every call is safe on every device: `availability(feature)` maps `checkFeatureStatus()` to `AVAILABLE / DOWNLOADABLE / UNAVAILABLE`, and `summarize`/`describeImage` return a typed `AiResult` (`Success / Unavailable / Failed`) instead of throwing. On the ~99% of devices without the feature, the client reports `UNAVAILABLE` and the UI shows nothing extra - no crash, no degraded layout. `CancellationException` is rethrown, matching the repository's error-handling rule.

**Why the contract lives in `:core:ai`, not `:core:domain`.** On-device AI is inherently platform-bound (AICore, `Bitmap`). Forcing it into the pure-Kotlin domain via a `ByteArray` round-trip would be an abstraction with no payoff. Features depend on `:core:ai` (a core module, allowed by the module rules); tests still swap the binding through Metro `replaces`, so testability is unchanged. The device-bound `MlKitOnDeviceAiClient` is excluded from coverage like the other hardware-backed implementations and is covered by a fake in the ViewModel tests.

**Cost-aware by design.** Summarization is user-triggered (a "Summarize" chip shown only on capable devices), so no inference runs unless asked. Image description runs only when a screen reader is active (`AccessibilityManager.isTouchExplorationEnabled`) and the device supports it: sighted users pay nothing, while TalkBack users get an AI alt-text richer than the bare title. The bitmap is loaded once through the shared Coil cache (`allowHardware(false)` so ML Kit can read pixels), not re-fetched. Translation follows the same restraint: the detail screen offers it only when language identification finds the description is in a language other than the device's, and the model download happens on the first tap, not on load.

**Other on-device AI the SDK offers** (next increments are a pick, not research):
- *Gemini Nano tier (flagship-gated, ML Kit GenAI):* Proofreading and Rewriting (a seller listing composer), and the Prompt API for custom tasks - attribute extraction from descriptions, category classification, search-suggestion generation. The Prompt API is the recommended next step.
- *Broad on-device tier (classic ML Kit, far beyond flagships):* Entity Extraction (addresses/phones into action chips), Image Labeling / Object Detection (auto-tag photos, visual search), Smart Reply. (Translation and Language ID already ship.)
- *Custom-model tier:* LiteRT (TF Lite) + MediaPipe with NNAPI/GPU delegates for on-device embeddings - semantic and visual search.

## Performance

The main scroll jank risk in a Compose grid is per-item allocation during recomposition. Two changes had the most impact:

1. **`remember(imageUrl, context)` on `ImageRequest`** with a fixed decode size (400x400). Without this, Coil creates a new request object per item per recomposition and waits for layout to resolve the target size. With it, the request is built once per URL and decodes at the exact resolution the API serves.

2. **`@Immutable` on UI models + `contentType` on grid items.** `@Immutable` lets the Compose compiler skip recomposition for items whose data hasn't changed. `contentType` helps `LazyVerticalGrid` recycle composables within the same layout type, avoiding teardown/rebuild when scrolling. The trade-off: `@Immutable` is a contract the compiler trusts but doesn't enforce at runtime. `ItemUiModel` only has `String`, `Int`, `Double`, and `Boolean` fields, so the contract holds, but adding a mutable field later would silently break it.

Other targeted fixes: `drawBehind` for gradient overlays (avoids a separate draw layer), file-level color/style constants (eliminates per-frame `Color.copy()` and `TextStyle.copy()` allocations), `LiveRegion` scoped to state-change views only (not the scrolling grid, where it causes accessibility service notifications on every scroll event).

**Startup and edge-to-edge.** `MainActivity` calls `enableEdgeToEdge()` so the app draws behind the system bars (which `targetSdk 35+` enforces); the `Scaffold`-based screens consume the insets, and the grid adds the bottom system-bar inset so the last row clears the nav bar. A `:baselineprofile` module (the `androidx.baselineprofile` plugin on a `com.android.test` project) generates a baseline profile by exercising cold startup, the grid scroll, and opening a detail. The profile is generated on a Gradle-managed virtual device (`./gradlew :app:generateBaselineProfile`), so CI needs no physical hardware, and `androidx.profileinstaller` installs it at runtime to AOT-compile the startup and scroll paths.

## Deeplinks and Share

For a marketplace, organic sharing is a primary growth channel. A shared link should land the receiver directly on the item, not on the home screen. Share links include an 8-character installation ID (truncated UUID, persisted in SharedPreferences): `socialapp://item/1?ref=a3b2c1d0`. The `ref` parameter enables share attribution without collecting PII.

The app registers both `socialapp://` (custom scheme, works immediately) and `https://socialapp.app/` (App Links, requires `assetlinks.json` hosted on the domain in production). Navigation Compose handles deeplink parsing via `navDeepLink` on the route definition.

## Quality Gates

- **Lint:** Custom `lint.xml` enforcing hardcoded text, accessibility, and security as errors. A checked-in baseline tracks pre-existing non-actionable issues; new violations fail the build.
- **Coverage:** Kover with line and branch minimums. `./gradlew koverVerify` runs as part of the quality check.
- **EditorConfig:** 4-space indent, 120-char lines, UTF-8, LF endings.

## At Scale

- **DI-aggregated navigation.** Features currently expose `NavGraphBuilder` extensions that `:app` calls explicitly. The next step contributes each feature's nav registration into a Metro multibinding (`@ContributesIntoSet`) so `:app` iterates the set and names no feature at all; deferred for now because wiring inter-feature navigation callbacks through a pure multibinding is awkward.
- **`:feature:*:api` / `:impl` split** so `:app` depends only on the api/nav surface and feature implementations aggregate at runtime. Worth it once `:app` build times bite.
- **State persistence** for ephemeral list state (search query, filter, grid mode), which is currently lost on process death. The ViewModels resolve through the metrox factory, which builds them from a plain `Provider` map with no `SavedStateHandle` plumbing; threading `SavedStateHandle` through that factory is the prerequisite, then the Store persists and restores screen state.
- **Offline-first** with Room cache replacing the current in-memory `cachedItems` in `ItemRepositoryImpl`. The current cache is process-lifetime only with no invalidation beyond `forceRefresh`. At scale, Room with a sync timestamp provides persistence across app restarts and offline browsing, and folds in request single-flight (concurrent `forceRefresh` callers share one in-flight request) and Paging 3.
- **Snapshot testing** (Paparazzi) for visual regression across screen states.
- **Observability backend.** The `CrashReporter` seam and `BreadcrumbInterceptor` are in place with a Logcat binding; swapping in a vendor SDK (Crashlytics, Sentry) and wiring `mapping.txt` upload from CI turns the seam into real production telemetry.
- **More on-device AI.** The ML Kit GenAI Prompt API is the recommended next increment (structured attribute extraction, category classification); see [On-device AI](#on-device-ai) for the full menu.

Already landed from this list: the Store interceptor seam (now consumed by `:core:observability`), the first on-device AI features (summarization, image description, and translation in `:core:ai`), and edge-to-edge plus baseline-profile generation (`:baselineprofile`).

## Running the Project

See the [README](README.md#build-test-release) for build, test, and release commands, the mock server, and deep-link testing.

## Onboarding

**Adding a feature:**
1. Create `feature/name/` with `data/` and `presentation/`
2. Define the repository interface in `domain`, implement it in `data`, annotate the impl `@ContributesBinding(AppScope::class)`
3. Build the ViewModel with `Store<State, Event>`, annotate it `@ContributesIntoMap(AppScope::class)` (assisted-injected if it takes a nav arg), call the repository directly
4. Write the Composable, collect state with `collectAsStateWithLifecycle`
5. Register the route in `Navigation.kt`
6. Write ViewModel tests with a fake repository

**Conventions:**
- **State is a `sealed interface`.** Every screen state is an explicit variant, no nullable fields. Why: the `when` expression is exhaustive at compile time. Adding a new state forces handling it everywhere. Nullable fields create implicit states that the compiler can't check.
- **Events are fire-and-forget.** Navigation, share intents, snackbars. Why: events represent side effects that should not be replayed on configuration change. SharedFlow with no replay ensures this. If an event needs to survive recomposition, it belongs in state instead.
- **Repository interfaces live in domain, implementations in data.** Why: domain defines the contract, data implements it. This means domain has zero Android framework dependencies, which makes it unit-testable without Robolectric and extractable into a pure Kotlin module.
- **Tests use fakes, not Mockito.** Why: mocks couple tests to method call sequences. Refactoring the repository's internal calls breaks mock-based tests even when behavior is unchanged. Fakes test the contract.
- **UI models are separate from domain models.** Why: domain models carry business data (raw price as `Double`). UI models carry display-ready data (formatted price as `String`). Mixing them either leaks formatting into the domain or raw data into the UI layer.
- **All strings in `strings.xml`.** Why: EU accessibility and localization requirements. Hardcoded text is a lint error in the project config.
- **`modifier` is the first optional parameter.** Why: Compose convention enforced by lint. Callers expect `modifier` in a predictable position for chaining.
- **Errors are typed (`ErrorType`), not raw strings.** Why: the data layer should not know how to phrase a user-facing message. Typed errors let the UI resolve to localized strings and let tests assert on error categories without depending on specific wording.
