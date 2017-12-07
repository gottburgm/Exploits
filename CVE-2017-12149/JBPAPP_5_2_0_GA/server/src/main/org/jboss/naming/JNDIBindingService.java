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
package org.jboss.naming;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A service which allows arbitrary values to be bound into JNDI.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public class JNDIBindingService
{
   /** The JNDI bindings managed by this service */
   private JNDIBindings bindings;

   /** The root context name under which the values are bound */
   private String rootName;

   public JNDIBindings getBindings()
   {
      return bindings;
   }
   public void setBindings(JNDIBindings bindings)
   {
      this.bindings = bindings;
   }

   public String getRootName()
   {
      return rootName;
   }
   public void setRootName(String rootName)
   {
      this.rootName = rootName;
   }

   public void addBindings() throws NamingException
   {
      Context ctx = new InitialContext();
      if( rootName != null )
         ctx = (Context) ctx.lookup(rootName);

      JNDIBinding[] values = bindings.getBindings();
      for(int n = 0; n < values.length; n ++)
      {
         String name = values[n].getName();
         Object value;
         try
         {
            value = values[n].getValue();
         }
         catch(Exception e)
         {
            NamingException ne = new NamingException("Failed to obtain value from binding: "+name);
            ne.setRootCause(e);
            throw ne;
         }
         Util.bind(ctx, name, value);
      }
      ctx.close();
   }
   public void removeBindings() throws NamingException
   {
      Context ctx = new InitialContext();
      if( rootName != null )
         ctx = (Context) ctx.lookup(rootName);

      JNDIBinding[] values = bindings.getBindings();
      for(int n = 0; n < values.length; n ++)
      {
         String name = values[n].getName();
         Util.unbind(ctx, name);
      }
      ctx.close();
   }

   public void start() throws Exception
   {
      addBindings();
   }
   public void stop() throws Exception
   {
      removeBindings();
   }
}
