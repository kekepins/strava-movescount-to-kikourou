rmdir release /s /q

C:\devtools\jkdk19_64\bin\jlink --module-path C:/devtools/jkdk19_64/jmods;mods --add-modules kikstrava --launcher start-app=kikstrava/kikstrava.KikStravaGui --output release --strip-debug --compress 2 --no-header-files --no-man-pages

copy template\kikstrava.bat release\
copy template\stravakik.conf release\conf\