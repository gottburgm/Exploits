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
import org.jboss.util.file.Files;

/**
 * Class to execute shell scripts asynchronously and collect output
 * 
 * @author Richard Achmatowicz
 * @version $Revision: 1.0
 */
public class AsyncShellScriptExecutor extends AbstractShellScriptExecutor {

	/* variables for controlling use of shutdown.sar */
	boolean useShutdown = false ;
	String pathToShutdownSar = null ;
	String deployDir = null ;

	public boolean isRunning() {
		return process != null ;
	}		
	public boolean getUseShutdown() {
		return useShutdown ;
	}
	public void setUseShutdown(boolean useShutdown) {
		this.useShutdown = useShutdown ;
	}
	public String getPathToShutdownSar() {
		return pathToShutdownSar ;
	}
	public void setPathToShutdownSar(String pathToShutdownSar) {
		this.pathToShutdownSar = pathToShutdownSar ;
	}
	public String getDeployDir() {
		return deployDir ;
	}
	public void setDeployDir(String deployDir) {
		this.deployDir = deployDir ;
	}
	public void startShellCommand(String[] commandArray) throws Exception {
		startShellCommand(commandArray, null, null) ;
	}
	public void startShellCommand(String[] commandArray, String[] envp) throws Exception {
		startShellCommand(commandArray, envp, null) ;
	}    
	public void startShellCommand(String[] commandArray, String[] envp, File workingDir) throws Exception {

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

		/* if we don't give the pumpers a little time, we can miss lines */
		try {
			Thread.sleep(100) ;
		}
		catch(InterruptedException ie) {
		}
		/* return control to the caller */
	}

	/*
	 * Join and wait
	 */
	public void joinShellCommand() {
		boolean terminated = false ;

		/* check for tetrmination of the command */
		int exitCode = 0 ;
		try {
			exitCode = process.exitValue() ;
			terminated = true ;
		}
		catch (IllegalThreadStateException itse) {
			System.out.println("Process not yet terminated - waiting for termination (possibly forever)") ;
			// only wait if the stuff wa not found? The process has terminated already?
			try {
				exitCode = process.waitFor();
				terminated = true ;
			} catch (InterruptedException ie) {
				System.out.println("Error in wait") ;
			}
		}
		if (terminated) {
			System.out.println("Process terminated exit code = " + exitCode) ;	    
		}	

		/* close the streams here */
		outlog.close() ;
		errorlog.close() ;
		closeAllStreams(process) ;

		/* indicate process is no longer running */
		process = null ;
	}

	/*
	 * Join with timeout
	 */
	public void joinShellCommand(int timeout) {
		int timeLeft = timeout ;
		boolean terminated = false ;

		/* check for termination of the command */
		int exitCode = 0 ;
		try {
			exitCode = process.exitValue() ;
			terminated = true ;
		}
		catch (IllegalThreadStateException itse) {
			/* the command has not terminated, wait for timeout seconds */
			System.out.println("Process not yet terminated - waiting for " + timeout + " seconds") ;
			while (timeLeft > 0) {
				try {
					exitCode = process.exitValue();
					terminated = true ;
					break ;
				} catch (IllegalThreadStateException itse2) {
					System.out.println("Still waiting...") ;
					
					try {
						Thread.sleep(1*1000) ;
					}
					catch(InterruptedException ie) {
					}
					timeLeft-- ;
				}
			}

			/* the command has not terminated, we need to kill it (somehow) */
			if (!terminated) {
				/* we are running a server instance via a shell */
				if (useShutdown) {			
					try {
						// shutdownServer(getDeployURL("shutdown.sar").getPath(), getDeployDir()) ;
						shutdownServer(getPathToShutdownSar(), getDeployDir()) ;
					}
					catch(IOException e) {
						/* we have got to do something about this case... */
						/* short of getting the pid of the shell and sending a signal, I don't know */
						System.out.println("Critical error: can't shut down server: " + e.getMessage()) ;
					}
				}
				/* now kill the shell process if it is still active */
				try {
					exitCode = process.exitValue();
				} catch (IllegalThreadStateException itse3) {
					System.out.println("Shell command did not terminate - destroying shell process...command may still be running") ;
					// close the streams first
					outlog.close() ;
					errorlog.close() ;
					closeAllStreams(process) ;
					// kill the shell process
					process.destroy() ;
				}
			}
		}

		if (terminated) {
			System.out.println("Process terminated with exit code " + exitCode) ;
		}

		/* close the streams here */
		outlog.close() ;
		errorlog.close() ;
		closeAllStreams(process) ;

		/* indicate process is no longer running */
		process = null ;
	}		

	/* 
	 * Shutdown a server by deploying shutdown.sar into its deploy directory
	 * and then deleting it.
	 * 
	 * This is about the only way we can kill a server started with run.sh/run.bat
	 * due to the difficulty of: 
	 * (i) discovering the process id of the shell or the server started by the shell
	 * (ii) sending a signal to that process
	 * on both Windows and UNIX. 
	 */
	private void shutdownServer(String pathToShutdownSar, String deployDir) throws IOException {

		final int SHUTDOWN_DEPLOY_TIME = 5 ;
		final int SHUTDOWN_UNDEPLOY_TIME = 10 ;

		if (pathToShutdownSar == null || deployDir == null) {
			throw new IOException("Skipping deployment of shutdown.sar: path to shutdown and deploy dir are not set") ;
		}

		/* first try to shutdown the server instance */
		System.out.println("Shell command did not terminate - deploying shutdown.sar...") ;
		String shutdownSource = null ;
		String shutdownTarget = null ;
		try {
			shutdownSource = pathToShutdownSar ;
			shutdownTarget = deployDir + "/shutdown.sar" ;
			// System.out.println("source = " + source) ;
			// System.out.println("target = " + target) ;
                        Files.copy(new File(shutdownSource), new File(shutdownTarget));
		}
		catch(Exception e) {
			throw new IOException("Problem deploying shutdown: " + e.getMessage()) ;
		} ;

		// give shutdown.sar time to deploy
		try {
			Thread.sleep(SHUTDOWN_DEPLOY_TIME*1000) ;
		}
		catch(InterruptedException ie) {
		}

		// now undeploy shutdown.sar
		File shutdown = new File(shutdownTarget) ;
		if (!shutdown.exists()) {
			throw new IOException("Problem undeploying shutdown: shutdown.sar file not found") ;
		}
		shutdown.delete() ;

		// give shutdown.sar time to undeploy (and cause the AS to exit)
		try {
			Thread.sleep(SHUTDOWN_UNDEPLOY_TIME*1000) ;
		}
		catch(InterruptedException ie2) {
		}
	}
}
