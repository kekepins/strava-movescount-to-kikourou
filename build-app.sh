#!/usr/bin/env bash
#
# Construit un livrable macOS AUTONOME de KikStrava via jpackage :
#   - target/dist/KikStrava.app     (application avec runtime Java + JavaFX embarques)
#   - target/dist/KikStrava-*.dmg   (image disque distribuable)
#
# Aucun Java n'est requis sur la machine cible : tout est embarque.
# Le fichier de configuration se trouve dans l'app sous Contents/app/conf/stravakik.conf
# (template vide a remplir : kikourou, strava, garmin).
#
# Prerequis : JDK >= 17 avec jpackage (fourni par le JDK), Maven.
# NB : jpackage produit un livrable pour la plateforme courante (ici macOS).
#      Pour Windows, relancer ce type de build sur une machine Windows.
#
set -euo pipefail
cd "$(dirname "$0")"

SETTINGS="/Users/Shared/apache-maven-3.8.4/conf/settings.xml"
MVN=(mvn -s "$SETTINGS")
APP_NAME="KikStrava"
MAIN_CLASS="kikstrava.KikStravaGui"

echo ">> Build du jar-with-dependencies..."
"${MVN[@]}" -q clean package

JAR="$(ls target/*-jar-with-dependencies.jar | head -1)"
JAR_NAME="$(basename "$JAR")"

# JavaFX : classifier natif selon l'architecture reelle de la JVM.
M2_REPO="$("${MVN[@]}" -q help:evaluate -Dexpression=settings.localRepository -DforceStdout 2>/dev/null | tail -1)"
JFX_V="$("${MVN[@]}" -q help:evaluate -Dexpression=javafx.version -DforceStdout 2>/dev/null | tail -1)"
JVM_ARCH="$(java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/os.arch/{print $2; exit}' | tr -d ' ')"
if [ "$JVM_ARCH" = "aarch64" ] || [ "$JVM_ARCH" = "arm64" ]; then CLS="mac-aarch64"; else CLS="mac"; fi
jfx() { echo "$M2_REPO/org/openjfx/javafx-$1/$JFX_V/javafx-$1-$JFX_V-$CLS.jar"; }
FXMP="$(jfx base):$(jfx graphics):$(jfx controls):$(jfx fxml)"
echo ">> JavaFX $JFX_V ($CLS)"

# Preparer l'input jpackage : le jar + un dossier conf (template vide).
WORK="target/jpackage-input"
rm -rf "$WORK" && mkdir -p "$WORK/conf"
cp "$JAR" "$WORK/"
cp template/stravakik.conf "$WORK/conf/stravakik.conf"
grep -q "garmin.apikey" "$WORK/conf/stravakik.conf" || \
  printf '\n# information garmin (cle API personnelle)\ngarmin.apikey=\n' >> "$WORK/conf/stravakik.conf"

OUT="target/dist"
rm -rf "$OUT" && mkdir -p "$OUT"

echo ">> jpackage app-image..."
jpackage --type app-image --name "$APP_NAME" \
  --input "$WORK" \
  --main-jar "$JAR_NAME" \
  --main-class "$MAIN_CLASS" \
  --module-path "$FXMP" --add-modules javafx.controls,javafx.fxml \
  --java-options "-Dfile.encoding=UTF-8" \
  --dest "$OUT"

# Ajouter le dossier conf au classpath de l'app (jpackage ne met que le main-jar).
CFG="$OUT/$APP_NAME.app/Contents/app/$APP_NAME.cfg"
if ! grep -q 'app.classpath=$APPDIR/conf' "$CFG"; then
  # inserer une 2e ligne app.classpath juste apres la premiere
  awk '1; /^app.classpath=\$APPDIR\// && !done {print "app.classpath=$APPDIR/conf"; done=1}' "$CFG" > "$CFG.tmp" && mv "$CFG.tmp" "$CFG"
fi

echo ">> jpackage dmg..."
jpackage --type dmg --name "$APP_NAME" \
  --app-image "$OUT/$APP_NAME.app" \
  --dest "$OUT" 2>/dev/null || echo "(dmg optionnel : echec non bloquant)"

echo ">> Livrables dans $OUT :"
ls -la "$OUT"
echo
echo "Config a remplir : $OUT/$APP_NAME.app/Contents/app/conf/stravakik.conf"
