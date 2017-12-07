/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.scripts.support;

import java.lang.InterruptedException ;
import java.io.BufferedReader ;
import java.io.InputStreamReader ;
import java.io.PrintWriter ;
import java.io.StringWriter ;
import java.io.File ;
import java.io.IOException ;

import org.jboss.test.scripts.support.AbstractShellScriptExecutor ;

/**
 * Class to execute shell scripts synchronously and collect output
 * 
 * @author Richard Achmatowicz
 * @version $Revision: 1.0
 */
public class ShellScriptExecutor extends AbstractShellScriptExecutor {

	public void runShellCommand(String[] commandArray) throws Exception {
		runShellCommand(commandArray, null, null) ;
	}
	public void runShellCommand(String[] commandArray, String[] envp) throws Exception {
		runShellCommand(commandArray, envp, null) ;
	}    
	public void runShellCommand(String[] commandArray, String[] envp, File workingDir) throws Exception {

		/* create strings to write output to */
		outWriter = new StringWriter() ;
		outlog = new PrintWriter(outWriter,true);
		errWriter = new StringWriter() ;
		errorlog = new PrintWriter(errWriter,true);

		/* transparency */
		writeShellCommand(commandArray) ;

		try {
			process = Runtime.getRuntime().exec(commandArray, envp, workingDir);
		} catch (IOException ioe) {
			System.err.println("Could not start command."+ ioe);
			return;
		}

		/* open the streams here */ 
		final BufferedReader inStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
		final BufferedReader errStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));

		/* start pumping output from process to files */
		Thread outPump = new OutputPumper(inStream, outlog);
		outPump.start();
		Thread errPump = new OutputPumper(errStream, errorlog);
		errPump.start();

		/* check for tetrmination of the command */
		int exitCode = 0 ;
		try {
			exitCode = process.exitValue() ;
			// System.out.println("exit code = " + exitCode) ;
		}
		catch (IllegalThreadStateException itse) {
			System.out.println("Process not yet terminated - waiting") ;
			// only wait if the stuff wa not found? The process has terminated
			// already?
			try {
				process.waitFor();
			} catch (InterruptedException ie) {
				System.out.println("Error in wait") ;
			}
		}

		/* if we don't give the pumpers a little time, we can miss lines */
		try {
			Thread.sleep(1000) ;
		}
		catch(InterruptedException ie) {
		}

		/* close the streams here */
		outlog.close() ;
		errorlog.close() ;
		closeAllStreams(process) ;
	}
}
