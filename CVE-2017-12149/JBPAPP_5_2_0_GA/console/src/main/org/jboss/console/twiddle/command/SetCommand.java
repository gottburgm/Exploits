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

import java.beans.PropertyEditor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.common.beans.property.BeanUtils;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.util.Strings;


/**
 * Set the values of one MBean attribute.
 *
 * @author heiko.rupp@cellent.de
 * @version $Revision: 113110 $
 */
public class SetCommand 
	extends MBeanServerCommand
{

	private ObjectName objectName;
	private List attributeNames = new ArrayList(5);	
	private boolean prefix = true;	
	private String query;

	public SetCommand()
	{
		super("set", "Set the value of one MBean attribute");
	}


   public void displayHelp()
   {
		PrintWriter out = context.getWriter();

		out.println(desc);
		out.println();
		out.println("usage: " + name + " [options] <name> <attr> <val>");
		out.println("options:");
		out.println("    --noprefix    Do not display attribute name prefixes");
		out.println("    --            Stop processing options");

		out.flush();
   }
   
	private boolean processArguments(final String[] args)
		throws CommandException
	{
		log.debug("processing arguments: " + Strings.join(args, ","));

		if (args.length == 0)
		{
			throw new CommandException("Command requires arguments");
		}

		String sopts = "-:";
		LongOpt[] lopts =
			{
				new LongOpt("noprefix", LongOpt.NO_ARGUMENT, null, 0x1000),
			};

		Getopt getopt = new Getopt(null, args, sopts, lopts);
		getopt.setOpterr(false);

		int code;
		int argidx = 0;

		while ((code = getopt.getopt()) != -1)
		{
			switch (code)
			{
				case ':':
					throw new CommandException
						("Option requires an argument: " + args[getopt.getOptind() - 1]);

				case '?':
					throw new CommandException
						("Invalid (or ambiguous) option: " + args[getopt.getOptind() - 1]);

				case 0x1000:
					prefix = false;
					break;
                  
					// non-option arguments
				case 1:
					{
						String arg = getopt.getOptarg();

						switch (argidx++)
						{
							case 0:
								objectName = createObjectName(arg);
								log.debug("mbean name: " + objectName);
								break;

							default:
								log.debug("adding attribute name: " + arg);
								attributeNames.add(arg);
								break;
						}
						break;
					}
			}
		}

		return true;
	}
   

   public void execute(String[] args) throws Exception
   {

		processArguments(args);
				
		String theAttr = (String)attributeNames.toArray()[0]; 
		String theVal = (String)attributeNames.toArray()[1]; 

		if (objectName == null)
			throw new CommandException("Missing object name");

		log.debug("attribute names: " + attributeNames);
		if (attributeNames.size() != 2 )
		{
			throw new CommandException("Wrong number of arguments");
		}
		MBeanServerConnection server = getMBeanServer();

		MBeanInfo info = server.getMBeanInfo(objectName);
		
		MBeanAttributeInfo[] attrs = info.getAttributes();
		
		MBeanAttributeInfo attr=null;

		boolean found = false;
		for (int i=0;i < attrs.length ; i++ ) 
		{			
			if (attrs[i].getName().equals(theAttr) && 
				attrs[i].isWritable()) {
				
					found=true;
					attr= attrs[i];
					break;
				}
		}
		
		if (found == false)
		{
			
			throw new CommandException("No matching attribute found");
		}
		else 
		{
			Object oVal = convert(theVal,attr.getType());
			Attribute at = new Attribute(theAttr,oVal);
			server.setAttribute(objectName,at);
			
			// read the attribute back from the server
			if (!context.isQuiet())
			{
				PrintWriter out = context.getWriter();
				Object nat = server.getAttribute(objectName,theAttr);
				if (nat==null)
					out.println("null");
				else 
					if (prefix)
						out.print(theAttr+"=");
					out.println(nat.toString());
			}
		}
		

   }

	/**
	 * Convert val into an Object of type type
	 * @param val The given value
	 * @param oType the wanted return type
	 * @return the value in the correct representation
	 * @throws Exception various :)
	 */
	private Object convert (String val,String oType) throws Exception
	{
		PropertyEditor editor = PropertyEditorFinder.getInstance().find(BeanUtils.findClass(oType));
		editor.setAsText(val);
		return editor.getValue();		
	}
}
