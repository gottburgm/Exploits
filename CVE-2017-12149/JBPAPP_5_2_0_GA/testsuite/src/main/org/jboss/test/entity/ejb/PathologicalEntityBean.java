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
package org.jboss.test.entity.ejb;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

import org.jboss.test.entity.interfaces.Pathological;
import org.jboss.test.entity.interfaces.PathologicalEntityHome;
import org.jboss.test.entity.interfaces.PathologicalEntity;

/**
 * A Bad entity.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class PathologicalEntityBean
   implements EntityBean
{
   private EntityContext entityContext;

   private String name;

   public String getName()
   {
      pathological();
      return name;
   }

   public String getSomething()
   {
      pathological();
      return "Something";
   }

   public void setSomething(String value)
   {
      pathological();
   }
	
   public String ejbCreate(String name)
      throws CreateException
   {
      pathological();
      this.name = name;
      return name;
   }
	
   public void ejbPostCreate(String name)
      throws CreateException
   {
   }

   public String ejbFindByPrimaryKey(String name)
   {
      pathological();
      return name;
   }
	
   public void ejbActivate()
   {
      pathological();
   }
	
   public void ejbLoad()
   {
      pathological();
   }
	
   public void ejbPassivate()
   {
      pathological();
   }
	
   public void ejbRemove()
      throws RemoveException
   {
      pathological();
   }
	
   public void ejbStore()
   {
      pathological();
   }
	
   public void setEntityContext(EntityContext context)
   {
      pathological();
      entityContext = context;
   }
	
   public void unsetEntityContext()
   {
      pathological();
      entityContext = null;
   }

   private void pathological()
   {
      if (Pathological.isPathological())
         throw new Error("Handle this correctly please");
   }
}
