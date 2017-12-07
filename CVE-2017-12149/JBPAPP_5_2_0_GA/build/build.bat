@echo off

REM  ======================================================================
REM
REM  This is the main entry point for the build system.
REM
REM  Users should be sure to execute this file rather than 'ant' to ensure
REM  the correct version is being used with the correct configuration.
REM
REM  ======================================================================
REM
REM $Id: build.bat 61858 2007-03-29 17:19:26Z dimitris@jboss.org $

REM ******************************************************
REM Ignore the ANT_HOME variable: we want to use *our*
REM ANT version and associated JARs.
REM ******************************************************
REM Ignore the users classpath, cause it might mess
REM things up
REM ******************************************************

SETLOCAL

set NOPAUSE=true
set CLASSPATH=
set ANT_HOME=

set ANT_OPTS=-Xmx256m -Dbuild.script=build.bat

REM ******************************************************
REM - "for" loops have been unrolled for compatibility
REM   with some WIN32 systems.
REM ******************************************************

set NAMES=tools;tools\ant;tools\apache\ant
set SUBFOLDERS=..;..\..;..\..\..;..\..\..\..

REM ******************************************************
REM ********Save the command line arguments***************
REM ******************************************************
set BUILD_ARGS=%1
if ""%1""=="""" goto completedArgs
shift
:processArg
if ""%1""=="""" goto completedArgs
set BUILD_ARGS=%BUILD_ARGS% %1
shift
goto processArg

:completedArgs

REM ******************************************************
REM ******************************************************

SET EXECUTED=FALSE
for %%i in (%NAMES%) do call :subLoop %%i %BUILD_ARGS%

goto :EOF

REM ******************************************************
REM ********* Search for names in the subfolders *********
REM ******************************************************

:subLoop
for %%j in (%SUBFOLDERS%) do call :testIfExists %%j\%1\bin\ant.bat %BUILD_ARGS%

goto :EOF

REM ******************************************************
REM ************ Test if ANT Batch file exists ***********
REM ******************************************************

:testIfExists
if exist %1 call :BatchFound %1 %BUILD_ARGS%

goto :EOF

REM ******************************************************
REM ************** Batch file has been found *************
REM ******************************************************

:BatchFound
if (%EXECUTED%)==(FALSE) call :ExecuteBatch %1 %BUILD_ARGS%
set EXECUTED=TRUE

goto :EOF

REM ******************************************************
REM ************* Execute Batch file only once ***********
REM ******************************************************

:ExecuteBatch
echo Calling %1 %BUILD_ARGS%
call %1 %BUILD_ARGS%

:end

if "%NOPAUSE%" == "" pause

