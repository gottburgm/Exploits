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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/** The EJBLocal proxy for an entity

 @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 @version $Revision: 81030 $
 */
class EntityProxy extends LocalProxy
   implements InvocationHandler
{
   static final long serialVersionUID = 5196148608172665115L;
   private Object cacheKey;
   private boolean removed;

   EntityProxy(String jndiName, Object id, BaseLocalProxyFactory factory)
   {
      super(jndiName, factory);
      cacheKey = id;
   }

   protected Object getId()
   {
      return cacheKey;
   }

   public final Object invoke(final Object proxy, final Method m, Object[] args)
      throws Throwable
   {
      if(removed)
      {
         throw new javax.ejb.NoSuchObjectLocalException("The instance has been removed: " + toStringImpl());
      }

      if (args == null)
         args = EMPTY_ARGS;

      Object retValue = super.invoke( proxy, m, args );
      if( retValue == null )
      {
         // If not taken care of, go on and call the container
         retValue = factory.invoke(cacheKey, m, args);
      }

      if(m.equals(REMOVE))
      {
         removed = true;
      }

      return retValue;
   }

}
