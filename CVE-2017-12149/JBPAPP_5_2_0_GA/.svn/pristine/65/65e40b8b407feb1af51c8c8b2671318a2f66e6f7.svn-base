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

import java.io.BufferedReader ;
import java.io.PrintWriter ;
import java.io.StringWriter ;
import java.io.IOException ;

import junit.framework.Assert ;

/**
 * Base class to test command-line scripts.
 * 
 * @author Richard Achmatowicz
 * @version $Revision: 1.0
 */
public abstract class AbstractShellScriptExecutor {

	Process process = null ;
	StringWriter outWriter = null ;
	StringWriter errWriter = null ;
	PrintWriter outlog = null ;
	PrintWriter errorlog = null ;

	public String getOutput() {
		if (outWriter == null)
			return null ;
		return outWriter.toString() ;
	}
	public String getError() {
		if (errWriter == null)
			return null ;
		return errWriter.toString() ;
	}
	public void assertOnOutputStream(String string, String failureMessage) {
		if (getOutput().indexOf(string) == -1) {
			// assertion does not hold
			Assert.fail(failureMessage) ;
		}
	}
	public void assertOnErrorStream(String string, String failureMessage) {
		if (getError().indexOf(string) == -1) {
			// assertion does not hold
			Assert.fail(failureMessage) ;
		}
	}		
	protected void closeAllStreams(Process process)
	{
		try {
			process.getInputStream().close();
			process.getOutputStream().close();
			process.getErrorStream().close();
		}
		catch (IOException e) {
		}
	}   	
	protected void writeShellCommand(String[] command ) {
		// write the command to test case report
		System.out.print("ShellScriptExecutor: executing shell command -> ") ;
		for (int i = 0; i < command.length; i++) {
			System.out.print(command[i] + " ") ;
		}
		System.out.println() ;
	}

	/*
	 * Simple output pumper
	 */
	protected class OutputPumper extends Thread
	{
		private BufferedReader outputReader;
		private PrintWriter logWriter;
		public OutputPumper(BufferedReader outputReader, PrintWriter logWriter)
		{
			this.outputReader = outputReader;
			this.logWriter = logWriter;
		}
		public void run()
		{
			try {
				String line = null;
				while ((line = outputReader.readLine()) != null) {
					// System.out.println("Pumper trace:" +line) ;
					logWriter.println(line);
				}
			}
			catch (IOException e) {
			}
		}
	}
}
