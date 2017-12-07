/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.console.twiddle.command;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.jboss.util.Strings;

import java.io.PrintWriter;

/**
 * Query the server for a list of matching methods of MBeans.
 *
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 * @version <tt>$Revision: 81010 $</tt>
 */
public class ListDomainsCommand
		extends MBeanServerCommand {

	private boolean displayCount;

	public ListDomainsCommand() {
		super( "listDomains", "Query the server for a list of available domains" );
	}

	public void displayHelp() {
		PrintWriter out = context.getWriter();

		out.println( desc );
		out.println();
		out.println( "usage: " + name + " [options]" );
		out.println( "options:" );
		out.println( "    -c, --count    Display the domain count" );
		out.println( "    --             Stop processing options" );

		out.flush();
	}

	private void processArguments( final String[] args )
			throws CommandException {
		log.debug( "processing arguments: " + Strings.join( args, "," ) );

		String sopts = "-:c";
		LongOpt[] lopts =
				{
						new LongOpt( "count", LongOpt.NO_ARGUMENT, null, 'c' ),
				};

		Getopt getopt = new Getopt( null, args, sopts, lopts );
		getopt.setOpterr( false );

		int code;
		int argidx = 0;

		while ( ( code = getopt.getopt() ) != -1 ) {
			switch ( code ) {
				case ':':
					throw new CommandException
							( "Option requires an argument: " + args[getopt.getOptind() - 1] );

				case '?':
					throw new CommandException
							( "Invalid (or ambiguous) option: " + args[getopt.getOptind() - 1] );

					// non-option arguments
				case 1: {
					String arg = getopt.getOptarg();

					switch ( argidx++ ) {
						default:
							throw new CommandException( "Unused argument: " + arg );
					}
				}

				// Show count
				case 'c':
					displayCount = true;
					break;

			}
		}
	}

	public void execute( String[] args ) throws Exception {
		processArguments( args );

		String[] domains = getMBeanServer().getDomains();

		PrintWriter out = context.getWriter();

		if ( displayCount ) {
			out.println( domains.length );
		} else {
			for ( int i = 0; i < domains.length; i++ ) {
				out.println( domains[i] );				
			}
		}

		out.flush();
	}

}
