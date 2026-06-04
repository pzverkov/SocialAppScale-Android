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

At a larger scale, the Store becomes the point where interceptors plug in (logging, analytics, test state recording) without modifying each ViewModel.

## Module Structure

The `:core:*` layers and `:feature:itemlist` are Gradle modules behind convention plugins; itemdetail and favorite still live in `:app`.

```
:core:model      # Item, ErrorType (pure Kotlin, no deps)
:core:domain     # ItemRepository, FavoriteRepository, NetworkResult -> :core:model
:core:common     # Store<State, Event>, PriceFormatter (pure Kotlin)
:core:network    # OkHttp, Retrofit wiring
:core:ui         # components, theme, image loading -> :core:model
:core:sharing    # InstallationIdProvider, ShareLinkBuilder
:core:testing    # shared test doubles (fakes for the domain contracts) -> :core:domain

:feature:itemlist  # SocialAppApi, ItemRepositoryImpl, DTO, list screen + ViewModel
                   # -> :core:{model,domain,common,network,ui,sharing}

:app
└── com.pzverkov.socialapp/
    ├── core/di/           # Metro AppGraph, ViewModel factory
    ├── core/navigation/   # NavHost, routes, deep links
    ├── feature/
    │   ├── itemdetail/    # presentation (assisted-injected ViewModel)
    │   └── favorite/      # data (Room DB, DAO, entity), domain
    ├── MainActivity.kt
    └── SocialAppApplication.kt
```

`SocialAppApi` lives in `:feature:itemlist`, not in `:core:network`. Core provides infrastructure (the Retrofit instance, the OkHttp client); features own their API interfaces. The repository contracts sit in `:core:domain`, so itemdetail (still in `:app`) reaches item data through the interface, not through `:feature:itemlist`. No feature imports another.

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

## Performance

The main scroll jank risk in a Compose grid is per-item allocation during recomposition. Two changes had the most impact:

1. **`remember(imageUrl, context)` on `ImageRequest`** with a fixed decode size (400x400). Without this, Coil creates a new request object per item per recomposition and waits for layout to resolve the target size. With it, the request is built once per URL and decodes at the exact resolution the API serves.

2. **`@Immutable` on UI models + `contentType` on grid items.** `@Immutable` lets the Compose compiler skip recomposition for items whose data hasn't changed. `contentType` helps `LazyVerticalGrid` recycle composables within the same layout type, avoiding teardown/rebuild when scrolling. The trade-off: `@Immutable` is a contract the compiler trusts but doesn't enforce at runtime. `ItemUiModel` only has `String`, `Int`, `Double`, and `Boolean` fields, so the contract holds, but adding a mutable field later would silently break it.

Other targeted fixes: `drawBehind` for gradient overlays (avoids a separate draw layer), file-level color/style constants (eliminates per-frame `Color.copy()` and `TextStyle.copy()` allocations), `LiveRegion` scoped to state-change views only (not the scrolling grid, where it causes accessibility service notifications on every scroll event).

## Deeplinks and Share

For a marketplace, organic sharing is a primary growth channel. A shared link should land the receiver directly on the item, not on the home screen. Share links include an 8-character installation ID (truncated UUID, persisted in SharedPreferences): `socialapp://item/1?ref=a3b2c1d0`. The `ref` parameter enables share attribution without collecting PII.

The app registers both `socialapp://` (custom scheme, works immediately) and `https://socialapp.app/` (App Links, requires `assetlinks.json` hosted on the domain in production). Navigation Compose handles deeplink parsing via `navDeepLink` on the route definition.

## Quality Gates

- **Lint:** Custom `lint.xml` enforcing hardcoded text, accessibility, and security as errors. A checked-in baseline tracks pre-existing non-actionable issues; new violations fail the build.
- **Coverage:** Kover with line and branch minimums. `./gradlew koverVerify` runs as part of the quality check.
- **EditorConfig:** 4-space indent, 120-char lines, UTF-8, LF endings.

## At Scale

- **Remaining `:feature:*` modules.** `:feature:itemlist` is extracted via the `socialapp.android.feature` convention; `itemdetail` and `favorite` move next the same way, with a navigation contract so `:app` wires routes without depending on each feature's internals.
- **State persistence** for ephemeral list state (search query, filter, grid mode), which is currently lost on process death. At scale, the Store would persist and restore screen state automatically.
- **Store interceptors** for logging, analytics event tracking, and test state recording.
- **Snapshot testing** (Paparazzi) for visual regression across screen states.
- **Offline-first** with Room cache replacing the current in-memory `cachedItems` in `ItemRepositoryImpl`. The current cache is process-lifetime only with no invalidation beyond `forceRefresh`. At scale, Room with a sync timestamp provides persistence across app restarts and offline browsing.
- **Baseline profiles** for startup and scroll optimization.

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
