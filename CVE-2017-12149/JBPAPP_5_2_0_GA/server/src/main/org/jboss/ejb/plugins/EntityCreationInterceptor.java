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
package org.jboss.ejb.plugins;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.GlobalTxEntityMap;
import org.jboss.invocation.Invocation;
import org.jboss.tm.TxUtils;


/**
* The instance interceptors role is to break entity creation into two 
* calls, one for ejbCreate and one for ejbPostCreate. The ejbCreate
* method is passed over the invokeHome chain, and ejbPostCreate is 
* passed over the invoke chain.
*    
* @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
* @version $Revision: 81030 $
*/
public class EntityCreationInterceptor extends AbstractInterceptor
{
   public Object invokeHome(Invocation mi)
      throws Exception
   {
      // Invoke through interceptors
      Object retVal = getNext().invokeHome(mi);

      // Is the context now with an identity? 
      // This means that a create method was called, so invoke ejbPostCreate.
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) mi.getEnterpriseContext();
      if(ctx != null && ctx.getId() != null)
      {
         // copy from the context into the mi
         // interceptors down the chain look in the mi for the id not the ctx.
         mi.setId(ctx.getId());
         
         // invoke down the invoke chain
         // the final interceptor in EntityContainer will redirect this
         // call to postCreateEntity, which calls ejbPostCreate
         getNext().invoke(mi);

         // now it's ready and can be scheduled for the synchronization
         if(TxUtils.isActive(mi.getTransaction()))
         {
            GlobalTxEntityMap.NONE.scheduleSync(mi.getTransaction(), ctx);
         }
      }

      return retVal;
   }

   public Object invoke(Invocation mi)
      throws Exception
   {
      // nothing to see here... move along
      return getNext().invoke(mi);
   }
}

