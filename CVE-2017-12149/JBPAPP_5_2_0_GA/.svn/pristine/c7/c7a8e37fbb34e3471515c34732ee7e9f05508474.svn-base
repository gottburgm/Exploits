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

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Query the server for a list of matching methods of MBeans.
 *
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 * @version <tt>$Revision: 81010 $</tt>
 */
public class QueryMethodCommand
		extends MBeanServerCommand {

	private String query;

	private boolean displayCount;
	private String filter = "*:*";

	public QueryMethodCommand() {
		super( "queryMethod", "Query the server for a list of matching methods of MBeans" );
	}

	public void displayHelp() {
		PrintWriter out = context.getWriter();

		out.println( desc );
		out.println();
		out.println( "usage: " + name + " [options] <query>" );
		out.println( "options:" );
		out.println( "    -c, --count    Display the matching method count" );
		out.println( "    -f, --filter   Filter by domain" );
		out.println( "    --             Stop processing options" );
		out.println( "Examples:" );
		out.println( " query methods of all MBeans: " + name + " list" );
		out.println( " query all methods of all MBeans in the jboss domain: " + name + " -f \"jboss:*\" list" );

		out.flush();
	}

	private void processArguments( final String[] args )
			throws CommandException {
		log.debug( "processing arguments: " + Strings.join( args, "," ) );

		if ( args.length == 0 ) {
			throw new CommandException( "Command requires arguments" );
		}

		String sopts = "-:cf:";
		LongOpt[] lopts =
				{
						new LongOpt( "count", LongOpt.NO_ARGUMENT, null, 'c' ),
						new LongOpt( "filter", LongOpt.REQUIRED_ARGUMENT, null, 'f' ),
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
						case 0:
							query = arg;
							log.debug( "query: " + query );
							break;

						default:
							throw new CommandException( "Unused argument: " + arg );
					}
					break;
				}

				// Show count
				case 'c':
					displayCount = true;
					break;

				case 'f':
					filter = getopt.getOptarg();
					log.debug( "filter: " + filter );
					break;
			}
		}
	}

	public void execute( String[] args ) throws Exception {
		processArguments( args );

		if ( query == null )
			throw new CommandException( "Missing MBean method query" );


		// get the list of object names to work with
		ObjectName[] names = queryMBeans( filter );

		PrintWriter out = context.getWriter();

		if ( displayCount ) {
			out.println( getHits( names ) );
		} else {                    
			out.println( getResult(names) );
		}

		out.flush();
	}

	private String getResult( ObjectName[] names )
			throws IntrospectionException, InstanceNotFoundException, IOException, ReflectionException {
		MBeanOperationInfo[] ops;
		MBeanOperationInfo op;
		StringBuffer buffer = new StringBuffer();

		for ( int i = 0; i < names.length; i++ ) {
			ops = getMBeanServer().getMBeanInfo( names[i] ).getOperations();
			for ( int j = 0; j < ops.length; j++ ) {
				op = ops[j];
				if ( op.getName().toLowerCase().contains( query.toLowerCase() ) ) {
					buffer.append( getMBeanInfoString( names[i] ) )
							.append( " " ).append( getMethodInfoString( op ) )
							.append( "\n" );

				}
			}

		}
		return buffer.toString();

	}

	private int getHits( ObjectName[] names )
			throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
		MBeanOperationInfo[] ops;
		MBeanOperationInfo op;
		int hits = 0;
		for ( int i = 0; i < names.length; i++ ) {
			ops = getMBeanServer().getMBeanInfo( names[i] ).getOperations();
			for ( int j = 0; j < ops.length; j++ ) {
				op = ops[j];
				if ( op.getName().toLowerCase().contains( query.toLowerCase() ) ) {
					hits++;
				}
			}
		}
		return hits;
	}

	private String getMBeanInfoString( ObjectName name ) {
		return new StringBuffer().append( name.getDomain() )
				.append( ":" )
				.append( name.getKeyPropertyListString() )
				.append( " " ).toString();
	}

	private String getMethodInfoString( MBeanOperationInfo op ) {
		MBeanParameterInfo[] params = op.getSignature();
		StringBuffer sb = new StringBuffer()
				.append( op.getName() )
				.append( " " );

		for ( int k = 0; k < params.length; k++ ) {
			sb.append( params[k].getType() ).append( " " );
		}

		return sb.toString();
	}
}
