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
import java.io.FileReader;
import javax.management.ObjectName ;
import javax.management.MalformedObjectNameException ;



/**
 * Unit tests of run.sh and run.bat.
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
 * In order to avoid dependency on an untested shutdown.sh/shutdown.bat script, we start up run.sh/run.bat
 * using the AsyncShellScriptExecutor and shut it down using JMX.    
 * 
 * This test case should not be run with a server started by Server/Servermanager/ServerController.
 *    
 * @author Richard Achmatowicz
 * @version $Revision: $
 */
public class RunTestCase extends ScriptsTestBase
{
	private ObjectName SERVER_OBJ_NAME = null ;        
   private static final int STOP_TIMEOUT = Integer.parseInt(System.getProperty(SYSTEM_PROPERTY_JBOSSAS_SHUTDOWN_TIMEOUT, "30"));
   private static final int START_TIMEOUT = Integer.parseInt(System.getProperty(SYSTEM_PROPERTY_JBOSSAS_STARTUP_TIMEOUT, "120"));

   /**
    * Create a new RunTestCase.
    * 
    * @param name
    */
   public RunTestCase(String name)
   {
      super(name);     

      // init the server ON
      try {
    	  SERVER_OBJ_NAME = new ObjectName("jboss.system:type=Server") ;
      }
      catch(MalformedObjectNameException mfe) {
      }      
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
        File file = new File(getBinDir() + FS + "run" + suffix);

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
	   String command = "run" ;
	   String options = null ;  
	   String args = "-h" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 
	   
	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("usage: run","usage string not found in command output") ;
   }  
   
   /**
    * Tests run startup command
    * 
    * We check the following assertions in this test:
    * A1. error free startup of server
    * A2. bind address
    * A3. multicast address 
    * A4. ... 
    *  
    * @throws Exception
    */
   public void testNonLoopbackErrorFreeStartup() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "run" ;
	   String options = " -c " + getServerConfig() + " -b " + getServerHost()   ;  
	   String args = null ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // point to the server config we are going to use
	   // System.setProperty("jbosstest.server.config", "default") ;
	   
	   // execute command
	   getAsyncShellScriptExecutor().startShellCommand(shellCommand, envp, workingDir) ; 
	   
	   // waitForServerStart kills the process and throws an exception if server does not start 
	   try {
		   waitForServerStart(getAsyncShellScriptExecutor(), getServerHost(), START_TIMEOUT) ;
		   System.out.println("Server started successfully, START_TIMEOUT is " + START_TIMEOUT) ;
	   }
	   catch(IOException e) {
		   System.out.println("IOException: message = " + e.getMessage()) ;
		   writeLogsToTestCase() ;
		   
		   fail("Server failed to start in " + START_TIMEOUT + "seconds") ;
	   }
	   
	   // check assertions on the console output generated by the run command
	   getAsyncShellScriptExecutor().assertOnOutputStream("Started in","Started string not found in command output") ;
	   
	   // shutdown the server using JMX and the MBean server (jboss.system:type=Server
	   System.out.println("Calling shutdown") ;
	   getServer().invoke(SERVER_OBJ_NAME, "shutdown", new Object[0], new String[0]) ;
	   
	   // waitForServerStop kills the process and throws an exception if server does not stop 
	   try {
		   waitForServerStop(getAsyncShellScriptExecutor(), STOP_TIMEOUT) ;
		   System.out.println("Server stopped successfully") ;
	   }
	   catch(IOException e) {
		   System.out.println("IOException: message = " + e.getMessage()) ;
		   writeLogsToTestCase() ;
		   
		   fail("Server failed to stop") ;
	   }
   } 

   /**
    * Test whether the PIDFILE is correctly removed after stopping a server which
    * had been started in background ($RUN_JBOSS_IN_BACKGROUND) with $JBOSS_PIDFILE set
    * can run only on linux systems! (Solaris making trouble too, might be fixed in future)
    */
   public void testJBossInBackgroundPlusPidFile() throws Exception
   {
      if (!isLinux())
      {
          System.out.println("Not Linux, nothing to test.");
         return;
      }

      String command = "run";
      String options = null;
      String args = "-c production -b " + getServerHost();
      String[] shellCommand = getShellCommand(command, options, args);

      File pidFile;
      try
      {
         pidFile = File.createTempFile("jbosspidfile", null);
         pidFile.delete();   // we just need a suitable absolute path, the file will be created later
      }
      catch (IOException e)
      {
         fail("could not create a temporary file: " + e.getMessage());
         return;
      }

      // TODO Solaris machines tend to fail if ANY environment variables are given this way. Why?

      // set the environment
      String[] envp = new String[] { "LAUNCH_JBOSS_IN_BACKGROUND=true", "JBOSS_PIDFILE=" + pidFile.getAbsolutePath() };
      System.out.println("created pidFile: " + pidFile.getAbsolutePath());

      // set the working directory
      File workingDir = new File(getBinDir());

      // run+shutdown the server
      getAsyncShellScriptExecutor().startShellCommand(shellCommand, envp, workingDir);
      boolean invalidPidInFile = false;
      try
      {
         waitForServerStart(getAsyncShellScriptExecutor(), getServerHost(), START_TIMEOUT);
         System.out.println("Server started successfully, START_TIMEOUT is " + START_TIMEOUT);

         // check that the file exists
         assertTrue("pidFile doesn't exist.", pidFile.exists());

         FileReader pidReader = new FileReader(pidFile);
         char[] pidArray = new char[30];
         pidReader.read(pidArray);
         int pid = Integer.parseInt(new String(pidArray).trim());
         System.out.println("PID = " + pid);

         // check that this pid really exists
         Process p = Runtime.getRuntime().exec("kill -0 " + pid);
         p.waitFor(); // this will return non-zero exit value if no such pid exists
         assertTrue("Script wrote an invalid pid into pidfile", p.exitValue() == 0);

         Runtime.getRuntime().exec("kill -15 " + pid);
      }
      catch (NumberFormatException nfe)
      {
         invalidPidInFile = true;
         fail("could not parse PID from pidFile");
      }
      catch (Exception e)
      {
         System.out.println("Exception: message = " + e.getMessage());
         writeLogsToTestCase();
         fail("Server failed to start");
      }
      finally
      {
         if (invalidPidInFile)
         { // just an optimalization, waitForServerStop would do this anyway
            getServer().invoke(SERVER_OBJ_NAME, "shutdown", new Object[0], new String[0]);
         }
         waitForServerStop(getAsyncShellScriptExecutor(), STOP_TIMEOUT);
         System.out.println("Server stopped successfully");
         pidFile.deleteOnExit();
      }

      // check that the file is deleted
      Thread.sleep(2000);
      assertFalse("pidfile should be deleted", pidFile.exists());
   } 
   
   private void writeLogsToTestCase() {
	   
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
