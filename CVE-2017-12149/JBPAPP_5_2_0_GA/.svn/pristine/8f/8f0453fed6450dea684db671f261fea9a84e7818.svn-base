Test cases for command-line scripts
-----------------------------------

1. This directory ('scripts') contains automated tests for the command-line
scripts found in JBOSS_DIST/bin directory, including run.sh, shutdown.sh,
twiddle.sh and probe.sh. The tests also cover the case of the Windows-based
counterparts.

2. There are two test targets used to test these scripts:

tests-scripts:
- starts a JBoss instance called 'scripts' which is based on 'all'
- executes TwiddleTestCase, ProbeTestCase and WsclientTestCase against the instance
- stops the JBoss instance
- the server is started and stopped using JBoss Test

tests-scripts-noserver:
- makes use of a JBoss instance 'scripts-noserver' which is based on 'default'
- executes RunTestCase, ShutdownTestCase, WsconsumeTestCase, WsprovideTestCase,
WstoolsTestCase and PasswordToolTestCase.
- run.sh is used to start the servers
- JMX shutdown or shutdown.sh is used to stop the servers, depending on
the test case in question
- You can increase timeout for server startup and shutdown with these parameters
 -Djbosstest.jbossas.startup.timeout=300 -Djbosstest.jbossas.shutdown.timeout=45
 applies for RunTestCase and ShutdownTestCase

3. The commands and servers are executed using a small framework based
on the classes AbstractShellScriptExecutor, ShellScriptExecutor and
AsyncShellScriptExecutor. There is also a class for checking assertions
on server log files called LogFileAssertionChecker.

4. Due to the fact that tests-scripts-noserver involves starting and stopping
a potentially number of servers, these tests will exceed the normal junit.timeout
setting (3 minutes) and this needs to be extended. Also, it is wise to run this
target at the end of the test suite or separately.   