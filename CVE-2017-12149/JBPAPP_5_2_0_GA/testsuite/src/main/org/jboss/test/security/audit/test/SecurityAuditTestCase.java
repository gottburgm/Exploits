/**
 * 
 */
package org.jboss.test.security.audit.test;

import java.io.File;
import java.io.IOException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.test.scripts.test.ScriptsTestBase;

/**
 * This test case starts predefined configuration with audit logging enabled and asserts 
 * audit logfile for messages to see if successful audit. 
 * 
 * @author pskopek
 * @version $Revision: $
 */
public class SecurityAuditTestCase extends ScriptsTestBase {

	private ObjectName SERVER_OBJ_NAME = null ;
	private int START_TIMEOUT = 2400 ;
	private int STOP_TIMEOUT = 240 ;

	private String command = null;
	private String options = null;  
	private String args = null;
	private String[] shellCommand = null;
	   
	// environment 
	private String[] envp = null;
	// working directory
	private File workingDir = new File(getBinDir()) ;
	
	
	@Override
	protected void setUp() throws Exception {

		super.setUp();
		
		// Do not check for errors in the log files. Doesn't make sense when checking audit log. 
		setCheckLogsForErrors(false);
		
		command = "run" ;
		options = " -c " + getServerConfig() + " -b " + getServerHost();  
		args = null ;
		log.debug("command="+command+" options="+options+" args="+args);
		shellCommand = getShellCommand(command, options, args) ;

		// set the environment if necessary 
		envp = null ;
		// set the working directory
		workingDir = new File(getBinDir()) ;

	}

	
   /**
    * Create a new SecurityAuditTestCase.
    * 
    * @param name
    */
   public SecurityAuditTestCase(String name)
   {
      super(name);     
      
      // init the server ON
      try {
    	  SERVER_OBJ_NAME = new ObjectName("jboss.system:type=Server") ;
      }
      catch(MalformedObjectNameException mfe) {
    	  // re-throw the exception
    	  throw new RuntimeException(mfe);
      }      
   }

   
   
   /**
    * Prints out some basic info about the environment 
    */
   public void dumpExecutionEnvironment() {
	   String os = isWindows() ? "Windows" : "non-Windows" ;
	     
	   // dump out some basic config information
	   System.out.println("\nTesting run on " + os + " host") ;
	   System.out.println("Working directory: " + getBinDir()) ;
	   System.out.println("Dist directory: " + getDistDir()) ;	
	   System.out.println("Log directory: " + getLogDir()) ;
	   System.out.println("Server config: " + getServerConfig()) ;
   }
   
   /**
    * Tests if after successful startup and shutdown of server, audit log contains proper entries. 
    * @throws Exception
    */
   public void testServerStartupAndShutdownAuditMessage() throws Exception
   {
	   // execute command
	   getAsyncShellScriptExecutor().startShellCommand(shellCommand, envp, workingDir) ; 
	   getLog().debug("Starting the server ...");
	   
	   // waitForServerStart kills the process and throws an exception if server does not start 
	   try {
		   waitForServerStart(getAsyncShellScriptExecutor(), getServerHost(), START_TIMEOUT) ;
		   getLog().debug("Server started successfully");
	   }
	   catch(IOException e) {
		   getLog().error("IOException: message = " + e.getMessage()) ;
		   writeLogsToTestCase() ;
		   
		   fail("Server failed to start") ;
	   }
	   
	   // check audit message 
	   assertOnAuditLog(ScriptsTestBase.SERVER_STARTED_MESSAGE, "Audit log does not contain SERVER_STARTED_MESSAGE (" + ScriptsTestBase.SERVER_STARTED_MESSAGE + ")", false, true);
	   
	   // shutdown the server using JMX and the MBean server (jboss.system:type=Server
	   getLog().debug("Calling shutdown") ;
	   getServer().invoke(SERVER_OBJ_NAME, "shutdown", new Object[0], new String[0]) ;
	   
	   // waitForServerStop kills the process and throws an exception if server does not stop 
	   try {
		   waitForServerStop(getAsyncShellScriptExecutor(), STOP_TIMEOUT) ;
		   getLog().debug("Server stopped successfully") ;
	   }
	   catch(IOException e) {
		   getLog().error("IOException: message = " + e.getMessage()) ;
		   writeLogsToTestCase() ;
		   
		   fail("Server failed to stop") ;
	   }
	   
	   // check audit message 
	   assertOnAuditLog(ScriptsTestBase.SERVER_STOPPED_MESSAGE, "Audit log does not contain SERVER_STOPPED_MESSAGE (" + ScriptsTestBase.SERVER_STOPPED_MESSAGE + ")", false, true);
	   
	   
   }  

   private void writeLogsToTestCase() {
	   
	   // write the logs to output for diagnosis
	   System.out.println("============================== system.out ==============================") ;
	   System.out.println(getAsyncShellScriptExecutor().getOutput()) ;
	   System.out.println("============================== system.err ==============================") ;
	   System.out.println(getAsyncShellScriptExecutor().getError()) ;
	   System.out.println("========================================================================") ;	   
   }


}
