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
import java.io.FileReader ;
import java.io.IOException ;
import java.io.FileNotFoundException ;

/**
 * Class to read error logs in incremental fashion.
 * 
 * @author Richard Achmatowicz
 * @version $Revision: 1.0
 */
public class LogFileAssertionChecker {
	int checkpoint ;
	String filename = null ;
	BufferedReader bufferedReader = null ;

	public LogFileAssertionChecker(String filename) {
		checkpoint = 0 ;
		this.filename = filename ;
	}

	public void setCheckpoint(int checkpoint) {
		this.checkpoint = checkpoint ;
	}

	public int getCheckpoint() {
		return checkpoint ;
	}

	public boolean isStringInLog(String s, boolean useCheckpoint, boolean resetCheckpoint) {

		int linesRead = 0 ;
		String line = null ;
		boolean found = false ;
		
		openLogFile() ;
		try {
			// if use check point, move the file to the check point before reading
			if (useCheckpoint) {
				for (int i = 0; i < checkpoint; i++) {
					// read a line
					line = bufferedReader.readLine() ;
				}
			}
			// we are now at the last checkpoint in the file
			while ((line = bufferedReader.readLine()) != null) {
				// increment count of lines read
				linesRead++ ;

				// check if string in line
				if (line.indexOf(s) > -1) {
					// found the string
					found = true ;
					break ;
				}
			}
		}
		catch(IOException ioe) {
			System.out.println("LogFileAssertionChecker: error while reading log file: " + filename) ;
		}
		// we can reset the checkpoint if no further searches required
		if (resetCheckpoint) {
			// set new checkpoint
			checkpoint = checkpoint + linesRead ;
		}
		closeLogFile() ;
		return found ;
	}

	/*
	 * Open the log file for reading
	 */
	private void openLogFile() {
		try {
			bufferedReader = new BufferedReader(new FileReader(filename)) ;
		}
		catch(FileNotFoundException fnf) {
			System.out.println("The file " + filename + " was not found: ex=" + fnf.toString()) ;
		}
	}

	private void closeLogFile() {

		try {
			bufferedReader.close() ;
		}
		catch(IOException ioe) {
			System.out.println("The file " + filename + " could not be closed: ex=" + ioe.toString()) ;
		}
	}
}
