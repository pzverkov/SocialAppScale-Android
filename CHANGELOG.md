# Changelog

All notable changes are listed here, newest first. The format follows Keep a Changelog and versions follow semantic versioning. The tag, the release title, and `versionName` in `SocialApp-scale/app/build.gradle.kts` always match.

## [1.0.0] - 2026-11-16

First tagged release of the `SocialApp-scale` variant. It is a reference app, so the release APK is unsigned and ships with a build provenance attestation instead.

### Added
- Jetpack Compose marketplace app with item list and detail screens, debounced search, favorites in Room, and deep links with share attribution.
- Multi-module layout (`:core:*`, `:feature:*`) with Metro DI, convention plugins, and a build rule that rejects feature-to-feature and core-to-feature dependencies.
- On-device AI in `:core:ai`: description summarization and image description through Gemini Nano where the device supports it, and on-device translation everywhere.
- One shared OkHttp client for Retrofit and Coil, baseline profile generation, and a Store interceptor seam for crash breadcrumbs.
- Supply chain controls: actions pinned to commit SHAs, read-only workflow token, CodeQL on every pull request, Dependabot grouped by version family, a pinned Gradle distribution checksum, and one required gate check per variant.

### Known limits
- Cleartext HTTP to the local mock server is enabled for development (see SECURITY.md).
- The release APK is unsigned. Verify it with `gh attestation verify <apk> --repo pzverkov/SocialAppScale-Android`.

[1.0.0]: https://github.com/pzverkov/SocialAppScale-Android/releases/tag/v1.0.0
