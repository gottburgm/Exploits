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
package org.jboss.test.cmp2.keygen.ejb;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

/** The Integer pk based bean implementation
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public abstract class IntegerPKBean implements EntityBean
{
   public Integer ejbCreate(String value) throws CreateException
   {
      setValue(value);
      return null;
   }
   public void ejbPostCreate(String value)
   {
   }

   public abstract Integer getKey();
   public abstract void setKey(Integer key);
   public abstract String getValue();
   public abstract void setValue(String value);

   public void setEntityContext(EntityContext ctx)
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbLoad()
   {
   }

   public void ejbStore()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove() throws RemoveException
   {
   }

   public void unsetEntityContext()
   {
   }
}
