@echo off
set JLINK_VM_OPTIONS=-classpath conf/
set DIR=bin
"%DIR%\java" %JLINK_VM_OPTIONS% -m kikstrava/kikstrava.KikStravaGui %*

pause