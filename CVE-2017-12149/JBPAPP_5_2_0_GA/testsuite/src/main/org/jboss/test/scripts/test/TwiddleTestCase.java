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
import java.io.FileWriter;
import java.util.Properties;

/**
 * Unit tests of twiddle.sh and twiddle.bat.
 *
 * @author Richard Achmatowicz
 * @version $Revision: $
 */
public class TwiddleTestCase extends ScriptsTestBase
{
   /**
    * Create a new TwiddleTestCase.
    * 
    * @param name
    */
   public TwiddleTestCase(String name)
   {
      super(name);      
   }
   
   /**
    * Prints out some basic info about the environment 
    */
   public void testExecutionEnvironment() {
	   String os = isWindows() ? "Windows" : "non-Windows" ;
	     
	   // dump out some basic config information
	   System.out.println("\nTesting twiddle on " + os + " host") ;
	   System.out.println("Working directory: " + getBinDir()) ;
	   System.out.println("Dist directory: " + getDistDir()) ;	   
   }

    /**
     * Tests if IPv4 is forced to be used
     */
    public void testIPv4() {
        String suffix = isWindows() ? ".bat" : ".sh";
        String ipv4Text = "-Djava.net.preferIPv4Stack=true";
        File file = new File(getBinDir() + FS + "twiddle" + suffix);

        if (!fileContainsText(file, ipv4Text)) {
            fail("File " + file.getAbsolutePath()  + " doesn't contain '" + ipv4Text + "' text");
        }
    }

   /**
    * Tests twiddle "help" command (no args)
    *  
    * @throws Exception
    */
   public void testNoArgs() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "twiddle" ;
	   String options = "-s " + getJndiURL() ;  
	   String args = null ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // System.out.println("Output: " + getShellScriptExecutor().getOutput()) ;
	   // System.out.println("Error: " + getShellScriptExecutor().getError()) ;
	   
	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("usage: twiddle","usage string not found in command output") ;
   }  
   
   /**
    * Tests twiddle "jsr77" command
    *  
    * @throws Exception
    */
   public void testJSR77() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "twiddle" ;
	   String options = "-s " + getJndiURL() ;  
	   String args = "jsr77" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 
	   
	   // check assertions
	   // need to check that there are no errors in error log too! 
	   getShellScriptExecutor().assertOnOutputStream("J2EEDomain","J2EEDomain string not found in output") ;
   }  

   /**
    * Tests twiddle "xmbean" command (display xmbean descriptor for mbean)
    *  
    * @throws Exception
    */
   public void testXMbean() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "twiddle" ;
	   String options = "-s " + getJndiURL() ;  
	   String args = "xmbean jboss.system:type=Server" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("jboss.system:type=Server","ObjectName string not found in output") ;
   }   
   
   /**
    * Tests twiddle "info" command (display attributes and operations for mbean)
    *  
    * @throws Exception
    */
   public void testInfo() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "twiddle" ;
	   String options = "-s " + getJndiURL() ;  
	   String args = "info jboss.system:type=Server" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("Version","attribute 'Version' not found in output") ;
	   getShellScriptExecutor().assertOnOutputStream("void shutdown()","operation 'shutdown' not found in output") ;
   }   
   
   /**
    * Tests twiddle "get" command (gets an MBean attribute)
    *  
    * @throws Exception
    */
   public void testGet() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "twiddle" ;
	   String options = "-s " + getJndiURL() ;  
	   String args = "get jboss.system:type=Server Started" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("true","attribute value 'true' not found in output") ;
   }   
   
   /**
    * Tests twiddle "invoke" command (invoke an operation on an MBean)
    *  
    * @throws Exception
    */
   public void testInvoke() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "twiddle" ;
	   String options = "-s " + getJndiURL() ;  
	   String args = "invoke jboss:service=JNDIView list true" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("Global JNDI Namespace","category 'Global JNDI Namepspace' not found in output") ;
   }   
   
   /**
    * Tests twiddle "create" command (create an MBean)
    *  
    * @throws Exception
    * 
    */
   public void testCreate() throws Exception
   {
	   System.out.println("Help: Please implement me!") ;
   }   
   
   /**
    * Tests twiddle "setattrs" command (set the values of an MBean attribute)
    *  
    * @throws Exception
    */
   public void testSetattrs() throws Exception
   {
	   System.out.println("Help: Please implement me!") ;
   }   
   
   /**
    * Tests twiddle "unregister" command (unregister an MBean)
    *  
    * @throws Exception
    */
   public void testUnregister() throws Exception
   {
	   System.out.println("Help: Please implement me!") ;
   }   
  
   /**
    * Tests twiddle "query" command (query the server for a list of matching MBeans)
    *  
    * @throws Exception
    */
   public void testQuery() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "twiddle" ;
	   String options = "-s " + getJndiURL() ;  
	   String args = "query \"jboss.system:*\"" ;  // fix for Windows
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("jboss.system:type=ServerInfo","MBean 'jboss.system:type=ServerInfo' not found in output") ;
   }   
   
   /**
    * Tests twiddle "set" command (set the value of an MBean attribute)
    *  
    * @throws Exception
    */
   public void testSet() throws Exception
   {
	   System.out.println("Help: Please implement me!") ;
   }   
   
   /**
    * Tests twiddle "serverinfo" command (get information about the MBean server)
    *  
    * @throws Exception
    */
   public void testServerInfo() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "twiddle" ;
	   String options = "-s " + getJndiURL() ;  
	   String args = "serverinfo -l" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("jboss.system:type=Server","MBean 'jboss.system:type=Server' not found in output") ;
   }

    public void testPropertiesServerInfo() throws Exception
    {
        File propFile = new File("twiddle.properties");

        // set properties
        Properties props = new Properties();
        props.setProperty("twiddle.server", getJndiURL());
        props.store(new FileWriter(propFile), null);

        // build the shell command to execute
        // supply the command name prefix, any options as a string, and any args
        String command = "twiddle";
        String options = "-P " + propFile.getCanonicalPath();
        String args = "serverinfo -l";
        String[] shellCommand = getShellCommand(command, options, args) ;

        // set the environment if necessary
        String[] envp = null ;
        // set the working directory
        File workingDir = new File(getBinDir()) ;

        // execute command
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ;

        // check assertions
        getShellScriptExecutor().assertOnOutputStream("jboss.system:type=Server","MBean 'jboss.system:type=Server' not found in output") ;
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
