/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009 Red Hat Middleware, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.test.scripts.test;

import java.io.File ;
import java.io.IOException ;
import javax.management.ObjectName ;
import javax.management.MalformedObjectNameException ;



/**
 * Unit tests of shutdown.sh and shutdown.bat.
 *
 * Need to test the following features (in order of importance):
 * 1. error-free statup on non-loopback bind address 
 *    (-c <server> -b <non-loopback IP>)
 * 2. error-free startup on loopback bind address 
 *    (-c <server> without -b param)
 * 3. default server assignment (i.e. production)
 *    (-b <non-loopback IP>   
 * 4. options for configuring partition, multicast address and port 
 *    (-g <partition name> -u <mcast IP addr> -m <mcast port>)
 * 5. options for configuring startup directories 
 *    (-d <boot patch directory> -p <patch directory> -B <bootlib> -L <loader lib> -C <clapsspath lib>)
 * 6. help and version text
 *    (-h)
 *        
 * @author Richard Achmatowicz
 * @version $Revision: $
 */
public class ShutdownTestCase extends ScriptsTestBase
{
        private static final int STOP_TIMEOUT = Integer.parseInt(System.getProperty(SYSTEM_PROPERTY_JBOSSAS_SHUTDOWN_TIMEOUT, "30"));
        private static final int START_TIMEOUT = Integer.parseInt(System.getProperty(SYSTEM_PROPERTY_JBOSSAS_STARTUP_TIMEOUT, "120"));
		
   /**
    * Create a new ShutdownTestCase.
    * 
    * @param name
    */
   public ShutdownTestCase(String name)
   {
      super(name);     
   }
   
   
   /**
    * Prints out some basic info about the environment 
    */
   public void testExecutionEnvironment() {
	   String os = isWindows() ? "Windows" : "non-Windows" ;
	   // dump out some basic config information
	   System.out.println("\nTesting run on " + os + " host") ;
	   System.out.println("Working directory: " + getBinDir()) ;
	   System.out.println("Dist directory: " + getDistDir()) ;	
	   System.out.println("Log directory: " + getLogDir()) ;
	   System.out.println("Server config: " + getServerConfig()) ;
   }

    /**
     * Tests if IPv4 is forced to be used
     */
    public void testIPv4() {
        String suffix = isWindows() ? ".bat" : ".sh";
        String ipv4Text = "-Djava.net.preferIPv4Stack=true";
        File file = new File(getBinDir() + FS + "shutdown" + suffix);

        if (!fileContainsText(file, ipv4Text)) {
            fail("File " + file.getAbsolutePath()  + " doesn't contain '" + ipv4Text + "' text");
        }
    }

   /**
    * Tests run "help" command (no args)
    *  
    * @throws Exception
    */
   public void testNoArgs() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String[] shellCommand = getShellCommand("shutdown", null, "-h") ;
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 
	   
	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("usage: shutdown","usage string not found in command output") ;
   }  
   
   /**
    * Tests shutdown command with -S option
    * 
    * @throws Exception
    */
   public void testNonLoopbackShutdown() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String[] shellCommand = getShellCommand("run","-c " + getServerConfig() + " -b " + getServerHost(), null) ; 
	   String[] envp = null ;
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getAsyncShellScriptExecutor().startShellCommand(shellCommand, envp, workingDir) ; 
	   
	   // waitForServerStart kills the process and throws an exception if server does not start 
	   try {
		   waitForServerStart(getAsyncShellScriptExecutor(), getServerHost(), START_TIMEOUT) ;
		   System.out.println("Server started successfully") ;
	   }
	   catch(IOException e) {
		   System.out.println("IOException: message = " + e.getMessage()) ;
		   writeServerLogsToTestCase() ;
		   fail("Server failed to start") ;
	   }
	   
	   // check assertions on the console output generated by the run command
	   getAsyncShellScriptExecutor().assertOnOutputStream("Started in","Started string not found in command output") ;
	   
	   // shutdown the server using the shutdown command
	   String[] shutdownCommand = getShellCommand("shutdown", "-s " + getJndiURL() + " -S", null) ;
	   getShellScriptExecutor().runShellCommand(shutdownCommand, envp, workingDir) ;
	   
	   System.out.println("shutdown output = " + getShellScriptExecutor().getOutput()) ;
	   System.out.println("shutdown error = " + getShellScriptExecutor().getError()) ;
	   
	   // waitForServerStop kills the process and throws an exception if server does not stop 
	   try {
		   waitForServerStop(getAsyncShellScriptExecutor(), STOP_TIMEOUT) ;
		   System.out.println("Server stopped successfully") ;
		   getAsyncShellScriptExecutor().assertOnOutputStream(SERVER_STOPPED_MESSAGE,"Server shutdown message did not appear in logs") ;
	   }
	   catch(IOException e) {
		   System.out.println("IOException: message = " + e.getMessage()) ;
		   writeServerLogsToTestCase() ; 
		   fail("Server failed to stop") ;
	   }
   }  

   /* for the two following tests, see JBPAPP-3035 */
   
   /**
    * Tests shutdown command with -e option
    * 
    * @throws Exception
    *
   public void testNonLoopbackExit() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String[] shellCommand = getShellCommand("run","-c " + getServerConfig() + " -b " + getServerHost(), null) ; 
	   String[] envp = null ;
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getAsyncShellScriptExecutor().startShellCommand(shellCommand, envp, workingDir) ; 
	   
	   // waitForServerStart kills the process and throws an exception if server does not start 
	   try {
		   ScriptsTestBase.waitForServerStart(getAsyncShellScriptExecutor(), getServerHost(), START_TIMEOUT) ;
		   System.out.println("Server started successfully") ;
	   }
	   catch(IOException e) {
		   System.out.println("IOException: message = " + e.getMessage()) ;
		   writeServerLogsToTestCase() ;
		   fail("Server failed to start") ;
	   }
	   
	   // check assertions on the console output generated by the run command
	   getAsyncShellScriptExecutor().assertOnOutputStream("Started in","Started string not found in command output") ;
	   
	   // shutdown the server using the shutdown command
	   String[] shutdownCommand = getShellCommand("shutdown", "-s " + getJndiURL() + " -e 1", null) ;
	   getShellScriptExecutor().runShellCommand(shutdownCommand, envp, workingDir) ;
	   
	   System.out.println("shutdown output = " + getShellScriptExecutor().getOutput()) ;
	   System.out.println("shutdown error = " + getShellScriptExecutor().getError()) ;
	   
	   // waitForServerStop kills the process and throws an exception if server does not stop 
	   try {
		   ScriptsTestBase.waitForServerStop(getAsyncShellScriptExecutor(), STOP_TIMEOUT) ;
		   System.out.println("Server stopped successfully") ;
		   getAsyncShellScriptExecutor().assertOnOutputStream(SERVER_EXIT_MESSAGE,"Server exit message did not appear in logs") ;		   
	   }
	   catch(IOException e) {
		   System.out.println("IOException: message = " + e.getMessage()) ;
		   writeServerLogsToTestCase() ;
		   fail("Server failed to stop") ;
	   }
   } 
   */ 
   
   /**
    * Tests shutdown command with -H option
    * 
    * @throws Exception
    *
   public void testNonLoopbackHalt() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String[] shellCommand = getShellCommand("run","-c " + getServerConfig() + " -b " + getServerHost(), null) ; 
	   String[] envp = null ;
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getAsyncShellScriptExecutor().startShellCommand(shellCommand, envp, workingDir) ; 
	   
	   // waitForServerStart kills the process and throws an exception if server does not start 
	   try {
		   ScriptsTestBase.waitForServerStart(getAsyncShellScriptExecutor(), getServerHost(), START_TIMEOUT) ;
		   System.out.println("Server started successfully") ;
	   }
	   catch(IOException e) {
		   System.out.println("IOException: message = " + e.getMessage()) ;
		   writeServerLogsToTestCase() ;	   
		   fail("Server failed to start") ;
	   }
	   
	   // check assertions on the console output generated by the run command
	   getAsyncShellScriptExecutor().assertOnOutputStream("Started in","Started string not found in command output") ;
	   
	   // shutdown the server using the shutdown command
	   String[] shutdownCommand = getShellCommand("shutdown", "-s " + getJndiURL() + " -H 1", null) ;
	   getShellScriptExecutor().runShellCommand(shutdownCommand, envp, workingDir) ;
	   
	   // waitForServerStop kills the process and throws an exception if server does not stop 
	   try {
		   ScriptsTestBase.waitForServerStop(getAsyncShellScriptExecutor(), STOP_TIMEOUT) ;
		   System.out.println("Server stopped successfully") ;
		   getAsyncShellScriptExecutor().assertOnOutputStream(SERVER_HALTED_MESSAGE,"Server halted message did not appear in logs") ;		   
	   }
	   catch(IOException e) {
		   System.out.println("IOException: message = " + e.getMessage()) ;
		   writeServerLogsToTestCase() ;
		   fail("Server failed to stop") ;
	   }
   }  
   */
   
   public void writeServerLogsToTestCase() {
	   // write the logs to output for diagnosis
	   System.out.println("============================== system.out ==============================") ;
	   System.out.println(getAsyncShellScriptExecutor().getOutput()) ;
	   System.out.println("============================== system.err ==============================") ;
	   System.out.println(getAsyncShellScriptExecutor().getError()) ;
	   System.out.println("========================================================================") ;	   
   }
   /* 
    * one time setup mechamism
    * only good for static stuff
    *  
   public static Test suite() throws Exception
   {
	   TestSuite suite = new TestSuite();
	   suite.addTest(new TestSuite(TwiddleTestCase.class));
	   
	   JBossTestSetup setup = new JBossTestSetup(suite) {
		   
           protected void setUp() throws Exception {
           }
           
           protected void tearDown() throws Exception {
        	   
           }
	   } ;
      return setup ;
   }
   */
}
