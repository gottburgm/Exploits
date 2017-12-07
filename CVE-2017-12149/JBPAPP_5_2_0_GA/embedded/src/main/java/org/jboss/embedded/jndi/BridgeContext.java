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
package org.jboss.embedded.jndi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.naming.Context;
import javax.naming.NameNotFoundException;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
/** This class is the Context interface handler and performs the
    failed lookup delegation from the primary to secondary naming
    Context.
*/
public class BridgeContext implements InvocationHandler
{
   private Context primaryCtx;
   private Context secondaryCtx;

   public static Context createBridge(Context primaryCtx, Context secondaryCtx)
   {
      BridgeContext h = new BridgeContext(primaryCtx, secondaryCtx);
      Class[] interfaces = {Context.class};
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      return (Context) Proxy.newProxyInstance(loader, interfaces, h);
   }

   BridgeContext(Context primaryCtx, Context secondaryCtx)
   {
      this.primaryCtx = primaryCtx;
      this.secondaryCtx = secondaryCtx;
   }

   public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
   {
      Object value;
      // First try the primary context
      try
      {
         value = method.invoke(primaryCtx, args);
      }
      catch(InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         // Try the secondary if this is a failed lookup
         if( t instanceof NameNotFoundException && method.getName().startsWith("lookup") )
         {
            try
            {
               value = method.invoke(secondaryCtx, args);
            }
            catch (InvocationTargetException e1)
            {
               throw e1.getTargetException();
            }
         }
         else
         {
            throw t;
         }
      }
      return value;
   }
}
