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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class JndiBinder
{
   private String bindTo;
   private Object target;
   private boolean serializable;
   private Hashtable properties;

   public void setBindTo(String bindTo)
   {
      this.bindTo = bindTo;
   }

   public void setTarget(Object target)
   {
      this.target = target;
   }

   public void setSerializable(boolean serializable)
   {
      this.serializable = serializable;
   }

   public void setJndiProperties(Hashtable properties)
   {
      this.properties = properties;
   }

   public void start() throws Exception
   {
      InitialContext ctx1 = null;
      if (properties != null)
      {
         ctx1 = new InitialContext(properties);
      }
      else ctx1 = new InitialContext();
      InitialContext ctx = ctx1;

      try
      {
         if (serializable)
         {
            Util.rebind(ctx, bindTo, target);
         }
         else
         {
            NonSerializableFactory.rebind(ctx, bindTo, target);
         }
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind JndiBinder service into JNDI under jndiName:" + ctx.getNameInNamespace() + "/" + bindTo);
         namingException.setRootCause(e);
         throw namingException;
      }
   }

   public void stop() throws Exception
   {
      InitialContext ctx1 = null;
      if (properties != null)
      {
         ctx1 = new InitialContext(properties);
      }
      else ctx1 = new InitialContext();
      InitialContext ctx = ctx1;
      if (serializable)
      {
         Util.unbind(ctx, bindTo);
      }
      else
      {
         NonSerializableFactory.unbind(bindTo);
      }
   }
}
