@echo off
REM ============================================================================
REM  run.bat - Lance l'application kikstrava (JavaFX) sous Windows
REM  avec la configuration locale conf_local\stravakik.conf.
REM
REM  Prerequis : JDK >= 17 et Maven dans le PATH.
REM  Si tu utilises un settings.xml Maven specifique (repo d'entreprise),
REM  decommente et adapte la ligne MVN_SETTINGS ci-dessous.
REM ============================================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"

REM set MVN_SETTINGS=-s C:\chemin\vers\settings.xml
if not defined MVN_SETTINGS set MVN_SETTINGS=

echo ^>^> Compilation...
call mvn %MVN_SETTINGS% -Dplatform=win -q compile
if errorlevel 1 goto :error

echo ^>^> Construction du classpath...
call mvn %MVN_SETTINGS% -Dplatform=win -q dependency:build-classpath -Dmdep.outputFile=target\cp.txt >nul
if errorlevel 1 goto :error
set /p DEPS=<target\cp.txt

echo ^>^> Resolution du depot Maven et de la version JavaFX...
for /f "delims=" %%v in ('mvn %MVN_SETTINGS% -q help:evaluate -Dexpression^=settings.localRepository -DforceStdout') do set M2_REPO=%%v
for /f "delims=" %%v in ('mvn %MVN_SETTINGS% -q help:evaluate -Dexpression^=javafx.version -DforceStdout') do set JFX_V=%%v

set CLS=win
echo ^>^> JavaFX %JFX_V% (classifier %CLS%)

set FXDIR=%M2_REPO%\org\openjfx
set FXMP=%FXDIR%\javafx-base\%JFX_V%\javafx-base-%JFX_V%-%CLS%.jar;%FXDIR%\javafx-graphics\%JFX_V%\javafx-graphics-%JFX_V%-%CLS%.jar;%FXDIR%\javafx-controls\%JFX_V%\javafx-controls-%JFX_V%-%CLS%.jar;%FXDIR%\javafx-fxml\%JFX_V%\javafx-fxml-%JFX_V%-%CLS%.jar

REM Retirer les entrees openjfx du classpath general (JavaFX passe par --module-path).
REM Sous Windows on garde le classpath complet des deps non-JavaFX + nos classes + conf_local.
set CP=conf_local;target\classes;%DEPS%

echo ^>^> Lancement...
java --module-path "%FXMP%" --add-modules javafx.controls,javafx.fxml -cp "%CP%" kikstrava.KikStravaGui
goto :eof

:error
echo.
echo *** Echec. Verifie que Maven et le JDK sont dans le PATH, et le settings.xml si repo d'entreprise. ***
exit /b 1
