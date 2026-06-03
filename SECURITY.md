# Security Policy

SocialApp is a reference application, not a deployed product. If you find a security issue, please report it responsibly.

## Reporting a vulnerability

Use GitHub's private vulnerability reporting on this repository (Security tab -> Report a vulnerability). Do not open a public issue for a security report.

Expect an acknowledgement within a few days. This is a sample project maintained on a best-effort basis, so fixes do not follow a fixed SLA.

## Known, intentional dev-only settings

These are deliberate for a local sample and are not vulnerabilities. They must change before any real deployment, and each variant's README tracks them under "Known gaps":

- **Cleartext HTTP is enabled** (`usesCleartextTraffic="true"`) so the app can reach the local mock server. Production needs HTTPS and a `network-security-config`.
- **Signing material is local and disposable.** No private key ships in the repository; release builds stay unsigned until a developer generates a keystore from `keystore.properties.template`. A real release key belongs in a secret manager or a hardware-backed store.

## Scope

Reports should concern the application code. The mock server under each variant's `mock-server/` is a local development stub with no authentication by design, and is out of scope.
