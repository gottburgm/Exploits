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
package org.jboss.proxy.ejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.Handle;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.ejb.handle.StatefulHandleImpl;

/**
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 84403 $
 */
public class StatefulSessionInterceptor
   extends GenericEJBInterceptor
{
   /** Serial Version Identifier. @since 1.5 */
   private static final long serialVersionUID = -4333233488946091285L;

   /**
    * No-argument constructor for externalization.
    */
   public StatefulSessionInterceptor()
   {
   }

   // Public --------------------------------------------------------
   
   /**
    * InvocationHandler implementation.
    *
    * @throws Throwable    Any exception or error thrown while processing.
    */
   public Object invoke(Invocation invocation)
      throws Throwable
   {
      InvocationContext ctx = invocation.getInvocationContext();
      
      Method m = invocation.getMethod();
      
      // Implement local methods
      if (m.equals(TO_STRING))
      {
         return toString(ctx);
      }
      else if (m.equals(EQUALS))
      {
         Object[] args = invocation.getArguments();
         String argsString = args[0] != null ? args[0].toString() : "";
         String thisString = toString(ctx);
         return new Boolean(thisString.equals(argsString));
      }
      else if (m.equals(HASH_CODE))
      {
         return new Integer(ctx.getCacheId().hashCode());
      }
      // Implement local EJB calls
      else if (m.equals(GET_HANDLE))
      {
         int objectName = ((Integer) ctx.getObjectName()).intValue();
         String jndiName = (String) ctx.getValue(InvocationKey.JNDI_NAME);
         Invoker invoker = ctx.getInvoker();
         Object id = ctx.getCacheId();
         return createHandle(objectName, jndiName, invoker, id, ctx);
      }
      else if (m.equals(GET_EJB_HOME))
      {
         return getEJBHome(invocation);
      }
      else if (m.equals(GET_PRIMARY_KEY))
      {
         throw new RemoteException("Call to getPrimaryKey not allowed on session bean");
      }
      else if (m.equals(IS_IDENTICAL))
      {
         Object[] args = invocation.getArguments();
         String argsString = args[0].toString();
         String thisString = toString(ctx);
         return new Boolean(thisString.equals(argsString));
      }
      // If not taken care of, go on and call the container
      else
      {
         // It is a remote invocation
         invocation.setType(InvocationType.REMOTE);
         
         // On this entry in cache
         invocation.setId(ctx.getCacheId());
         
         return getNext().invoke(invocation);
      }
   }

   protected Handle createHandle(int objectName, String jndiName, Invoker invoker, 
         Object id, InvocationContext ctx)
   {
      return new StatefulHandleImpl(
            objectName, 
            jndiName, 
            invoker, 
            ctx.getInvokerProxyBinding(), 
            id,
            ctx.getValue("InvokerID"));
   }
   
   private String toString(InvocationContext ctx)
   {
      return ctx.getValue(InvocationKey.JNDI_NAME) + ":" + 
            ctx.getCacheId().toString();
   }
}
