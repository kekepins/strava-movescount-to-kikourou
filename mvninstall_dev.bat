@echo off

set JAVA_HOME=C:\devtools\jdk19_64
::set JAVA_HOME=C:\devtools\jdk18_64
set PATH="C:\devtools\apache-maven-3.5.2\bin";%PATH%

call mvn clean install 





