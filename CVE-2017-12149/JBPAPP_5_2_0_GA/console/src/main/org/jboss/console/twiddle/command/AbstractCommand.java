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

import org.jboss.util.NullArgumentException;

import org.jboss.logging.Logger;

/**
 * An abstract command.
 *
 * @version <tt>$Revision: 81010 $</tt>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 */
public abstract class AbstractCommand
   implements Command
{
   protected Logger log = Logger.getLogger(getClass());

   protected final String desc;
   
   protected final String name;

   protected CommandContext context;

   protected AbstractCommand(final String name, final String desc)
   {
      this.name = name;
      this.desc = desc;
   }
   
   public String getName()
   {
      return name;
   }

   public String getDescription()
   {
      return desc;
   }

   public void setCommandContext(final CommandContext context)
   {
      if (context == null)
         throw new NullArgumentException("context");
      
      this.context = context;
   }

   public void unsetCommandContext()
   {
      this.context = null;
   }
   
   /**
    * Return a cloned copy of this command.
    *
    * @return   Cloned command.
    */
   public Object clone()
   {
      try
      {
         return super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         throw new InternalError();
      }
   }
}
