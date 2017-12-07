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

/**
 * Unit tests of probe.sh and probe.bat.
 * 
 * Requires testing against the 'all' config which includes JGroups.
 * 
 * There are a number of tests which need covering and their priority:
 * (high) - that we get at least 4 responses (for the four channels in the AS) [DONE]
 * (high) - that -match DeafultPartition-JMS-CTRL returns only one response (need isolation, need negative assertion) [DONE]
 * (low)  - that -addr and -port when used in conjunction with server which has had 
 * its diagnostics address and port changed returns exactly 4 replies
 * 
 * @author Richard Achmatowicz
 * @version $Revision: $
 */
public class ProbeTestCase extends ScriptsTestBase
{
   /**
    * Create a new ProbeTestCase.
    * 
    * @param name
    */
   public ProbeTestCase(String name)
   {
      super(name);      
   }
   
   /**
    * Prints out some basic info about the environment 
    */
   public void testExecutionEnvironment() {
	   String os = isWindows() ? "Windows" : "non-Windows" ;
	     
	   // dump out some basic config information
	   System.out.println("\nTesting probe on " + os + " host") ;
	   System.out.println("Working directory: " + getBinDir()) ;
	   System.out.println("Dist directory: " + getDistDir()) ;	   
   }

    /**
     * Tests if IPv4 is forced to be used
     */
    public void testIPv4() {
        String suffix = isWindows() ? ".bat" : ".sh";
        String ipv4Text = "-Djava.net.preferIPv4Stack=true";
        File file = new File(getBinDir() + FS + "probe" + suffix);

        if (!fileContainsText(file, ipv4Text)) {
            fail("File " + file.getAbsolutePath()  + " doesn't contain '" + ipv4Text + "' text");
        }
    }

   /**
    * Tests probe "help" command 
    *  
    * @throws Exception
    */
   public void testNoArgs() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "probe" ;
	   String options = null ;  
	   String args = "-help" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("Probe [-help]","Usage string not found in command output") ;
   }  

   /**
    * Tests probe receives a response for every channel in the AS (AS 5)
    *  
    * @throws Exception
    */
   public void testResponseSet() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "probe" ;
	   String options = null ;  
	   String args = null ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("#4","Less than 4 channel responses found in command output") ;
	   getShellScriptExecutor().assertOnOutputStream("DefaultPartition-HAPartitionCache","DefaultPartition-HAPartitionCache channel response not found in command output") ;
	   getShellScriptExecutor().assertOnOutputStream("DefaultPartition-JMS-DATA","DefaultPartition-JMS-DATA channel response not found in command output") ;
	   getShellScriptExecutor().assertOnOutputStream("DefaultPartition-JMS-CTRL","DefaultPartition-JMS-CTRL channel response not found in command output") ;
	   getShellScriptExecutor().assertOnOutputStream("DefaultPartition","DefaultPartition channel response not found in command output") ;
   }  
   
   
   /**
    * Tests probe keys command (returns a list of keys which can be queried)
    *  
    * @throws Exception
    */
   public void testKeyKeys() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "probe" ;
	   String options = null ;  
	   String args = "keys" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("jmx","'jmx' string not found in command output") ;
	   getShellScriptExecutor().assertOnOutputStream("info","'info' string not found in command output") ;
	   getShellScriptExecutor().assertOnOutputStream("dump","'dump' string not found in command output") ;
   } 
   
   /**
    * Tests probe jmx=udp command (returns a list of JMX statistics for protocol udp)
    *  
    * @throws Exception
    */
   public void testKeyJMX() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "probe" ;
	   String options = null ;  
	   String args = "jmx" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   getShellScriptExecutor().assertOnOutputStream("UDP","'UDP' string not found in command output") ;
   }  
   
   /**
    * Tests probe -query props command (returns the stack used by each channel)
    *  
    * @throws Exception
    */
   public void testKeyDump() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "probe" ;
	   String options = null ;  
	   String args = "dump" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions        
	   getShellScriptExecutor().assertOnOutputStream("send probe on","'send probe on' string not found in command output") ;
   }  
   
   /**
    * Tests probe -match command (returns only responses containing the match text)
    *  
    * @throws Exception
    */
   public void testQueryMatch() throws Exception
   {
	   // build the shell command to execute
	   // supply the command name prefix, any options as a string, and any args
	   String command = "probe" ;
	   String options = null ;  
	   String args = "-match JMS-CTRL" ;
	   String[] shellCommand = getShellCommand(command, options, args) ;
	   
	   // set the environment if necessary 
	   String[] envp = null ;
	   // set the working directory
	   File workingDir = new File(getBinDir()) ;

	   // execute command
	   getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir) ; 

	   // check assertions
	   // need to check the absence of a string here!
	   // assertNotOnOutputStream("Default-Partition", "'Default-Partition' found in match list")
	   getShellScriptExecutor().assertOnOutputStream("DefaultPartition-JMS-CTRL","'DefaultPartition-JMS-CTRL' string not found in command output") ;
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
