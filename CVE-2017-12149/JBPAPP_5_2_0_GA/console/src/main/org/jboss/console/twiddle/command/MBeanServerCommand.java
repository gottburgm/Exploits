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

import java.util.Set;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;


/**
 * An abstract command to opperate on an MBeanServer.
 *
 * @version <tt>$Revision: 81010 $</tt>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 */
public abstract class MBeanServerCommand
   extends AbstractCommand
{
   protected MBeanServerCommand(final String name, final String desc)
   {
      super(name, desc);
   }

   protected ObjectName createObjectName(final String name)
      throws CommandException
   {
      try {
         return new ObjectName(name);
      }
      catch (MalformedObjectNameException e) {
         throw new CommandException("Invalid object name: " + name);
      }
   }
   
   protected MBeanServerConnection getMBeanServer()
   {
      return context.getServer();
   }
   
   protected ObjectName[] queryMBeans(final String query)
      throws Exception
   {
      // query the mbean server
      MBeanServerConnection server = getMBeanServer();
      
      Set matches = server.queryNames(new ObjectName(query), null);
      log.debug("Query matches: " + matches);

      if (matches.size() == 0) {
         throw new CommandException("No MBean matches for query: " + query);
      }

      ObjectName[] names =
         (ObjectName[])matches.toArray(new ObjectName[matches.size()]);

      return names;
   }
}
