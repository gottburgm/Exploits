@echo off
rem -------------------------------------------------------------------------
rem JBoss Password Tool
rem -------------------------------------------------------------------------

if not "%ECHO%" == ""  echo %ECHO%
if "%OS%" == "Windows_NT"  setlocal

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%
set PROGNAME=password_tool.bat
if "%OS%" == "Windows_NT" set PROGNAME=%~nx0%

set JAVA=%JAVA_HOME%\bin\java
if "%JBOSS_HOME%" == "" set JBOSS_HOME=%DIRNAME%\..

rem Setup the java endorsed dirs
set JBOSS_ENDORSED_DIRS=%JBOSS_HOME%\lib\endorsed

rem Setup the jboss password tool classpath
rem Shared libs
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JAVA_HOME%\lib\tools.jar
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\client\commons-logging.jar
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\client\jboss-logging-spi.jar
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\lib\endorsed\xalan.jar

rem Shared jaxb libs
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\client\activation.jar
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\client\jaxb-api.jar
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\client\jaxb-impl.jar
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\client\stax-api.jar

rem Specific dependencies
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\client\xmlsec.jar
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\client\jbosssx-client.jar
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\client\jbosssx-as-client.jar
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\lib\jbosssx.jar
set JBOSSPASS_CLASSPATH=%JBOSSPASS_CLASSPATH%;%JBOSS_HOME%\common\lib\log4j.jar

rem Execute the JVM
"%JAVA%" %JAVA_OPTS% "-Djava.endorsed.dirs=%JBOSS_ENDORSED_DIRS%" "-Dprogram.name=%PROGNAME%" -classpath "%JBOSSPASS_CLASSPATH%" org.jboss.security.integration.password.PasswordTool %*
