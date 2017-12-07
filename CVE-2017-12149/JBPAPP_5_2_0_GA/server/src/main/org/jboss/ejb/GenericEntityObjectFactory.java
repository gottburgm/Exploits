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
package org.jboss.ejb;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Entity EJBObject and EJBLocalObject proxy factories implement this generic interface.
 * The getEntityEJBObject method returns either EJBObject or EJBLoadlObject.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81030 $</tt>
 */
public interface GenericEntityObjectFactory
{
   Object getEntityEJBObject(Object id);

   class UTIL
   {
      private UTIL()
      {
      }

      public static Collection getEntityCollection(GenericEntityObjectFactory factory, Collection ids)
      {
         List result = new ArrayList();
         if(!ids.isEmpty())
         {
            for(Iterator i = ids.iterator(); i.hasNext();)
            {
               result.add(factory.getEntityEJBObject(i.next()));
            }
         }
         return result;
      }
   }
}
