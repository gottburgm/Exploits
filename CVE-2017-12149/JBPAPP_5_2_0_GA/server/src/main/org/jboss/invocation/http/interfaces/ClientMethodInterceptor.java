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
package org.jboss.invocation.http.interfaces;

import java.io.Externalizable;
import java.lang.reflect.Method;

import org.jboss.invocation.Invocation;
import org.jboss.proxy.Interceptor;

/** Handle toString, equals, hashCode locally on the client.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public class ClientMethodInterceptor extends Interceptor
   implements Externalizable
{
   /** The serialVersionUID. @since 1.1.4.1 */
   private static final long serialVersionUID = 3511654206312502958L;

   /** Handle methods locally on the client
    *
    * @param mi
    * @return
    * @throws Throwable
    */
   public Object invoke(Invocation mi) throws Throwable
   {
      Method m = mi.getMethod();
      String methodName = m.getName();
      HttpInvokerProxy proxy = (HttpInvokerProxy) mi.getInvocationContext().getInvoker();
      // Implement local methods
      if( methodName.equals("toString") )
      {
         return toString(proxy);
      }
      if( methodName.equals("equals") )
      {
         Object[] args = mi.getArguments();
         String thisString = toString(proxy);
         String argsString = args[0] == null ? "" : args[0].toString();
         return new Boolean(thisString.equals(argsString));
      }
      if( methodName.equals("hashCode") )
      {
         return (Integer) mi.getObjectName();
      }

      return getNext().invoke(mi);
   }

   private String toString(HttpInvokerProxy proxy)
   {
      StringBuffer tmp = new StringBuffer(proxy.toString());
      tmp.append('{');
      tmp.append("externalURLValue="+proxy.getExternalURLValue());
      tmp.append(",externalURL="+proxy.getExternalURL());
      tmp.append('}');
      return tmp.toString();
   }
}
