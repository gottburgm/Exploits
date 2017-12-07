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
package org.jboss.varia.autonumber;

import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.naming.InitialContext;

/**
 * ???
 * 
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @version $Revision: 81038 $
 */
public class AutoNumberEJB
   implements EntityBean
{
   public String name;
   public Integer value; 
   
   public String ejbCreate(String name) throws CreateException {
      this.name = name;
      this.value = new Integer(0);
      
      return null;
   }
   
   public void ejbPostCreate(String name) {}
   
   public Integer getValue()  {
      return value;
   }
   
   public void setValue(Integer value)  {
      this.value = value;
   }
   
   public void ejbActivate() {}
   public void ejbPassivate() {}
   public void ejbLoad() {}
   public void ejbStore() {}
   public void ejbRemove() {}
   public void setEntityContext(javax.ejb.EntityContext ec) {}
   public void unsetEntityContext() {}
}
