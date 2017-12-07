@echo off
rem -------------------------------------------------------------------------
rem JBoss Bootstrap Script for Windows
rem -------------------------------------------------------------------------

rem $Id: run.bat 113480 2012-08-28 16:41:26Z mbenitez $

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal enableextensions enabledelayedexpansion

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)
rem check the arguments for -r to set the config file
SET ARGS=
:LoopArgs
if "%1"=="" GOTO ContinueArgs
if "%1"=="-r" (
   rem set desired run.conf.bat
   SET RUN_CONF=%2
   SHIFT
   SHIFT
) else (
   rem keep to pass on to JBoss server
   SET ARGS=%ARGS% %1
   SHIFT
)
GOTO LoopArgs
:ContinueArgs

rem Read an optional configuration file.
if "x%RUN_CONF%" == "x" (
   set "RUN_CONF=%DIRNAME%run.conf.bat"
)
if exist "%RUN_CONF%" (
   echo "Calling %RUN_CONF%"
   call "%RUN_CONF%" %*
) else (
   echo "Config file not found %RUN_CONF%"
)

pushd %DIRNAME%..
if "x%JBOSS_HOME%" == "x" (
  set "JBOSS_HOME=%CD%"
)
popd

set DIRNAME=

if "%OS%" == "Windows_NT" (
  set "PROGNAME=%~nx0%"
) else (
  set "PROGNAME=run.bat"
)

REM Force use of IPv4 stack
set JAVA_OPTS=%JAVA_OPTS% -Djava.net.preferIPv4Stack=true

rem Setup JBoss specific properties
set JAVA_OPTS=%JAVA_OPTS% -Dprogram.name=%PROGNAME%

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
  if exist "%JAVA_HOME%\lib\tools.jar" (
    set "JAVAC_JAR=%JAVA_HOME%\lib\tools.jar"
  )
)

rem Add -server to the JVM options, if supported
"%JAVA%" -server -version 2>&1 | findstr /I hotspot > nul
if not errorlevel == 1 (
  set "JAVA_OPTS=%JAVA_OPTS% -server"
)

rem Add native to the PATH if present
set JBOSS_NATIVE_HOME=
set CHECK_NATIVE_HOME=
if exist "%JBOSS_HOME%\bin\libtcnative-1.dll" (
  set "CHECK_NATIVE_HOME=%JBOSS_HOME%\bin"
) else if exist "%JBOSS_HOME%\..\native\bin" (
  set "CHECK_NATIVE_HOME=%JBOSS_HOME%\..\native\bin"
) else if exist "%JBOSS_HOME%\bin\native\bin" (
  set "CHECK_NATIVE_HOME=%JBOSS_HOME%\bin\native\bin"
) else if exist "%JBOSS_HOME%\..\..\jboss-ep-5.2\native\bin" (
  set "CHECK_NATIVE_HOME=%JBOSS_HOME%\..\..\jboss-ep-5.2\native\bin"
)
if "x%CHECK_NATIVE_HOME%" == "x" goto WITHOUT_JBOSS_NATIVE

rem Translate to the absolute path

pushd "%CHECK_NATIVE_HOME%"
set JBOSS_NATIVE_HOME=%CD%
popd
set CHECK_JBOSS_NATIVE_HOME=
set JAVA_OPTS=%JAVA_OPTS% "-Djava.library.path=%JBOSS_NATIVE_HOME%;%PATH%;%SYSTEMROOT%"
set PATH=%JBOSS_NATIVE_HOME%;%PATH%;%SYSTEMROOT%

:WITHOUT_JBOSS_NATIVE
rem Find run.jar, or we can't continue

if exist "%JBOSS_HOME%\bin\run.jar" (
  if "x%JAVAC_JAR%" == "x" (
    set "RUNJAR=%JBOSS_HOME%\bin\run.jar"
  ) else (
    set "RUNJAR=%JAVAC_JAR%;%JBOSS_HOME%\bin\run.jar"
  )
) else (
  echo Could not locate "%JBOSS_HOME%\bin\run.jar".
  echo Please check that you are in the bin directory when running this script.
  goto END
)

rem If JBOSS_CLASSPATH empty, don't include it, as this will
rem result in including the local directory in the classpath, which makes
rem error tracking harder.
if "x%JBOSS_CLASSPATH%" == "x" (
  set "RUN_CLASSPATH=%RUNJAR%"
) else (
  set "RUN_CLASSPATH=%JBOSS_CLASSPATH%;%RUNJAR%"
)

set JBOSS_CLASSPATH=%RUN_CLASSPATH%

rem Setup JBoss specific properties

rem Setup the java endorsed dirs
set JBOSS_ENDORSED_DIRS=%JBOSS_HOME%\lib\endorsed

echo ===============================================================================
echo.
echo   JBoss Bootstrap Environment
echo.
echo   JBOSS_HOME: %JBOSS_HOME%
echo.
echo   JAVA: %JAVA%
echo.
echo   JAVA_OPTS: %JAVA_OPTS%
echo.
echo   CLASSPATH: %JBOSS_CLASSPATH%
echo.
echo ===============================================================================
echo.

:RESTART
"%JAVA%" %JAVA_OPTS% ^
   -Djava.endorsed.dirs="%JBOSS_ENDORSED_DIRS%" ^
   -classpath "%JBOSS_CLASSPATH%" ^
   org.jboss.Main %ARGS%

if ERRORLEVEL 10 goto RESTART

:END
if "x%NOPAUSE%" == "x" pause

:END_NO_PAUSE
