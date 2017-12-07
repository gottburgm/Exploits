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

import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 * An entity bean instance pool.
 *
 * @version <tt>$Revision: 81030 $</tt>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 */
public class EntityInstancePool
   extends AbstractInstancePool
{
   /**
    * Return an instance to the free pool. Reset state
    *
    * <p>Called in 3 cases:
    * <ul>
    *   <li>Done with finder method
    *   <li>Removed
    *   <li>Passivated
    * </ul>
    *
    * @param   ctx  
    */
   public void free(EnterpriseContext ctx)
   {
       // If transaction still present don't do anything (let the instance be GC)
       if (ctx.getTransaction() != null)
       {
          if( log.isTraceEnabled() )
             log.trace("Can Not FREE Entity Context because a Transaction exists.");
          return ;
       }

       super.free(ctx);
   }

   protected EnterpriseContext create(Object instance)
      throws Exception
   {
      return new EntityEnterpriseContext(instance, getContainer());
   }
}
