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
package org.jboss.proxy;

import java.io.Externalizable;
import java.lang.reflect.Method;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.Interceptor;

/**
 * Handle toString, equals, hashCode locally on the client.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.com
 * @version $Revision: 81030 $
 */
public class ClientMethodInterceptor extends Interceptor
   implements Externalizable
{
   /** The serialVersionUID. @since 1.1.2.1 */
   private static final long serialVersionUID = 6010013004557885014L;

   /** Handle methods locally on the client
    *
    * @param mi the invocation
    * @return the result of the invocation
    * @throws Throwable for any error
    */
   public Object invoke(Invocation mi) throws Throwable
   {
      Method m = mi.getMethod();
      String methodName = m.getName();
      // Implement local methods
      if (methodName.equals("toString"))
      {
         Object obj = getObject(mi);
         return obj.toString();
      }
      if (methodName.equals("equals"))
      {
         Object obj = getObject(mi);
         Object[] args = mi.getArguments();
         String thisString = obj.toString();
         String argsString = args[0] == null ? "" : args[0].toString();
         return new Boolean(thisString.equals(argsString));
      }
      if( methodName.equals("hashCode") )
      {
         Object obj = getObject(mi);
         return new Integer(obj.hashCode());
      }

      return getNext().invoke(mi);
   }

   /**
    * Get the object used in Object methods
    * 
    * @param mi the invocation
    * @return the object
    */
   protected Object getObject(Invocation mi)
   {
      Object cacheId = mi.getInvocationContext().getCacheId();
      if (cacheId != null)
         return cacheId;
      else
         return mi.getInvocationContext().getInvoker();
   }
   
}
