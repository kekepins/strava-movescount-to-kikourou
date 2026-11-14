@echo off
REM ============================================================================
REM  build-app.bat - Construit un livrable Windows AUTONOME de KikStrava
REM  via jpackage :
REM     target\dist\KikStrava\        (app-image : KikStrava.exe + runtime embarque)
REM     target\dist\KikStrava-*.msi   (installeur, si WiX Toolset est installe)
REM
REM  Aucun Java requis sur la machine cible : runtime Java + JavaFX embarques.
REM  La config se trouve dans app\conf\stravakik.conf (template vide a remplir :
REM  kikourou, strava, garmin).
REM
REM  Prerequis :
REM    - JDK >= 17 avec jpackage (fourni par le JDK) dans le PATH
REM    - Maven dans le PATH
REM    - Pour le .msi (optionnel) : WiX Toolset v3 (https://wixtoolset.org)
REM  Si tu utilises un settings.xml Maven specifique, adapte MVN_SETTINGS.
REM ============================================================================
set PATH="C:\devtools\apache-maven-3.9.6\bin";%PATH%

setlocal enabledelayedexpansion
cd /d "%~dp0"

REM set MVN_SETTINGS=-s C:\chemin\vers\settings.xml
if not defined MVN_SETTINGS set MVN_SETTINGS=

set APP_NAME=KikStrava
set MAIN_CLASS=kikstrava.KikStravaGui

echo ^>^> Build du jar-with-dependencies...
call mvn %MVN_SETTINGS% -Dplatform=win -q clean package
if errorlevel 1 goto :error

REM Trouver le jar-with-dependencies
for %%f in (target\*-jar-with-dependencies.jar) do set JAR=%%f
for %%f in ("%JAR%") do set JAR_NAME=%%~nxf
if not defined JAR goto :error

echo ^>^> Resolution depot Maven / version JavaFX...
for /f "delims=" %%v in ('mvn %MVN_SETTINGS% -q help:evaluate -Dexpression^=settings.localRepository -DforceStdout') do set M2_REPO=%%v
for /f "delims=" %%v in ('mvn %MVN_SETTINGS% -q help:evaluate -Dexpression^=javafx.version -DforceStdout') do set JFX_V=%%v

set CLS=win
set FXDIR=%M2_REPO%\org\openjfx
set FXMP=%FXDIR%\javafx-base\%JFX_V%\javafx-base-%JFX_V%-%CLS%.jar;%FXDIR%\javafx-graphics\%JFX_V%\javafx-graphics-%JFX_V%-%CLS%.jar;%FXDIR%\javafx-controls\%JFX_V%\javafx-controls-%JFX_V%-%CLS%.jar;%FXDIR%\javafx-fxml\%JFX_V%\javafx-fxml-%JFX_V%-%CLS%.jar
echo ^>^> JavaFX %JFX_V% (classifier %CLS%)

REM Preparer l'input jpackage : uniquement le jar
set WORK=target\jpackage-input
if exist "%WORK%" rmdir /s /q "%WORK%"
mkdir "%WORK%"
copy /y "%JAR%" "%WORK%\" >nul

set OUT=target\dist
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"

echo ^>^> jpackage app-image...
jpackage --type app-image --name "%APP_NAME%" ^
  --input "%WORK%" ^
  --main-jar "%JAR_NAME%" ^
  --main-class "%MAIN_CLASS%" ^
  --module-path "%FXMP%" --add-modules javafx.controls,javafx.fxml,jdk.httpserver,jdk.crypto.ec^
  --java-options "-Dfile.encoding=UTF-8" ^
  --win-console ^
  --dest "%OUT%"
if errorlevel 1 goto :error

REM Creer le dossier conf a la racine de l'app-image (un cran au-dessus de app\)
mkdir "%OUT%\%APP_NAME%\conf"
copy /y "template\stravakik.conf" "%OUT%\%APP_NAME%\conf\stravakik.conf" >nul
findstr /c:"garmin.apikey" "%OUT%\%APP_NAME%\conf\stravakik.conf" >nul || (
  echo.>> "%OUT%\%APP_NAME%\conf\stravakik.conf"
  echo # information garmin ^(cle API personnelle^)>> "%OUT%\%APP_NAME%\conf\stravakik.conf"
  echo garmin.apikey=>> "%OUT%\%APP_NAME%\conf\stravakik.conf"
)

REM Ajouter le dossier conf (situe a la racine via $ROOTDIR) au classpath de l'app
set CFG=%OUT%\%APP_NAME%\app\%APP_NAME%.cfg
findstr /c:"app.classpath=$ROOTDIR\conf" "%CFG%" >nul || (
  powershell -NoProfile -Command "(Get-Content -Raw '%CFG%') -replace '(app.classpath=\$APPDIR\\%JAR_NAME%)', ('$1' + [Environment]::NewLine + 'app.classpath=$ROOTDIR\conf') | Set-Content -NoNewline '%CFG%'"
)

echo ^>^> jpackage msi (optionnel, necessite WiX v3)...
jpackage --type msi --name "%APP_NAME%" ^
  --app-image "%OUT%\%APP_NAME%" ^
  --win-shortcut --win-menu ^
  --dest "%OUT%"
if errorlevel 1 (
  echo    ^(msi non genere : verifiez que WiX Toolset v3.14 est installe et dans le PATH. L'app-image reste utilisable.^)
)

echo.
echo ^>^> Livrables dans %OUT% :
dir /b "%OUT%"
echo.
echo Lancement : %OUT%\%APP_NAME%\%APP_NAME%.exe
echo Config a remplir : %OUT%\%APP_NAME%\conf\stravakik.conf
goto :eof

:error
echo.
echo *** Echec du build. Verifie JDK (jpackage) + Maven dans le PATH, et le settings.xml si repo d'entreprise. ***
exit /b 1
