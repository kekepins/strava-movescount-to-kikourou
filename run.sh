#!/usr/bin/env bash
#
# Lance l'application kikstrava (JavaFX) avec la configuration locale
# conf_local/stravakik.conf (identifiants reels : kikourou, strava, garmin).
#
# Choisit automatiquement le classifier natif JavaFX selon l'architecture
# REELLE de la JVM (os.arch) : gere le cas macOS Apple Silicon avec une JVM
# x86_64 lancee via Rosetta.
#
set -euo pipefail
cd "$(dirname "$0")"

SETTINGS="/Users/Shared/apache-maven-3.8.4/conf/settings.xml"
MVN=(mvn -s "$SETTINGS")

echo ">> Compilation..."
"${MVN[@]}" -q compile

echo ">> Construction du classpath..."
"${MVN[@]}" -q dependency:build-classpath -Dmdep.outputFile=target/cp.txt >/dev/null
DEPS="$(cat target/cp.txt)"

M2_REPO="$("${MVN[@]}" -q help:evaluate -Dexpression=settings.localRepository -DforceStdout 2>/dev/null | tail -1)"
JAVAFX_VERSION="$("${MVN[@]}" -q help:evaluate -Dexpression=javafx.version -DforceStdout 2>/dev/null | tail -1)"

JVM_ARCH="$(java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/os.arch/{print $2; exit}' | tr -d ' ')"
OS="$(uname -s)"
case "$OS" in
  Darwin) if [ "$JVM_ARCH" = "aarch64" ] || [ "$JVM_ARCH" = "arm64" ]; then CLASSIFIER="mac-aarch64"; else CLASSIFIER="mac"; fi ;;
  Linux)  if [ "$JVM_ARCH" = "aarch64" ]; then CLASSIFIER="linux-aarch64"; else CLASSIFIER="linux"; fi ;;
  *)      CLASSIFIER="win" ;;
esac
echo ">> JVM os.arch=$JVM_ARCH -> classifier JavaFX=$CLASSIFIER (v$JAVAFX_VERSION)"

jfx() { echo "$M2_REPO/org/openjfx/javafx-$1/$JAVAFX_VERSION/javafx-$1-$JAVAFX_VERSION-$CLASSIFIER.jar"; }
FXMP="$(jfx base):$(jfx graphics):$(jfx controls):$(jfx fxml)"

NONFX="$(printf '%s' "$DEPS" | tr ':' '\n' | grep -v openjfx | paste -sd: -)"
CP="conf_local:target/classes:$NONFX"

echo ">> Lancement..."
exec java \
  --module-path "$FXMP" \
  --add-modules javafx.controls,javafx.fxml \
  -cp "$CP" \
  kikstrava.KikStravaGui
