@echo off

rem ### =================================================================== ###
rem ##  jarcheck.bat                                                         ##
rem ##  A script for obtaining MD5 checksums on the JARs in a JBoss EAP      ##
rem ##  distribution.                                                        ##
rem ### =================================================================== ###

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal enableextensions enabledelayedexpansion

rem # Setup the current directory
if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

rem # Setup JBOSS_HOME and EAP_ROOT
pushd %DIRNAME%..
if "x%JBOSS_HOME%" == "x" (
  set "JBOSS_HOME=%CD%\"
)
popd

pushd %JBOSS_HOME%..
set "EAP_ROOT=%CD%"
popd

rem # Setup Java
if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set.
  echo Set JAVA_HOME to the directory of your local JVM to avoid this message.
  goto END
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

rem # Setup JARCHECK_JAR
set JARCHECK_JAR="%JBOSS_HOME%/bin/jarcheck.jar"
if not exist %JARCHECK_JAR% (
    echo "Missing required file: %JARCHECK_JAR%"
    goto END
)

rem # Run JARCheck
"%JAVA%" ^
   -jar "%JARCHECK_JAR%" ^
   %EAP_ROOT% > jarcheck-report.txt

:END