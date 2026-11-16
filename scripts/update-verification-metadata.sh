#!/bin/sh
# Regenerates gradle/verification-metadata.xml for a variant after a dependency bump.
# Usage: scripts/update-verification-metadata.sh SocialApp-scale
# Runs every task CI and the release workflow use, against fresh downloads, so the file covers them all.
# Gradle records only the host OS binaries (aapt2-*-osx.jar on a Mac). CI is Linux, so after a bump that
# changes the AGP version, add the aapt2 linux jar checksum from dl.google.com by hand.
set -eu
variant="${1:?usage: $0 SocialApp-basic|SocialApp-scale}"
cd "$(dirname "$0")/../$variant"
tasks="lintDebug testDebugUnitTest koverVerify assembleDebug assembleRelease assembleAndroidTest"
[ "$variant" = "SocialApp-scale" ] && tasks="$tasks :baselineprofile:assemble"
# shellcheck disable=SC2086
./gradlew --write-verification-metadata sha256 --refresh-dependencies --no-configuration-cache --continue $tasks
