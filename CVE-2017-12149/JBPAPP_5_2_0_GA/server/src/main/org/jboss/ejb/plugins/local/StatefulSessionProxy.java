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
package org.jboss.ejb.plugins.local;

import javax.ejb.EJBObject;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

/** The EJBLocal proxy for a stateful session

 @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 @version $Revision: 81030 $
 */
class StatefulSessionProxy extends LocalProxy
   implements InvocationHandler
{
   static final long serialVersionUID = -3113762511947535929L;
   private Object id;

   StatefulSessionProxy(String jndiName, Object id, BaseLocalProxyFactory factory)
   {
      super(jndiName, factory);
      this.id = id;
   }

   protected Object getId()
   {
      return id;
   }

   public final Object invoke(final Object proxy, final Method m,
      Object[] args)
      throws Throwable
   {
      if (args == null)
         args = EMPTY_ARGS;

      // The object identifier of a session object is, in general, opaque to the client. 
      // The result of getPrimaryKey() on a session EJBObject reference results in java.rmi.RemoteException.
      // The result of getPrimaryKey() on a session EJBLocalObject reference results in javax.ejb.EJBException.
      if (m.equals(GET_PRIMARY_KEY))
      {
         if (proxy instanceof EJBObject)
         {
            throw new RemoteException("Call to getPrimaryKey not allowed on session bean");
         }
         if (proxy instanceof EJBLocalObject)
         {
            throw new EJBException("Call to getPrimaryKey not allowed on session bean");
         }
      }

      Object retValue = super.invoke( proxy, m, args );
      if (retValue == null)
      {
         // If not taken care of, go on and call the container
         retValue = factory.invoke(id, m, args);
      }

      return retValue;
   }
}
