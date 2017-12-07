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
package org.jboss.test.cmp2.fkstackoverflow.ejb;

import org.jboss.logging.Logger;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.ejb.EJBException;


/**
 * @ejb:bean
 *    type="Stateless"
 *    name="Facade"
 *    view-type="remote"
 * @ejb.util generate="physical"
 * @ejb:transaction type="Required"
 * @ejb:transaction-type type="Container"
 */
public class FacadeSessionBean
   implements SessionBean
{
   private static Logger log = Logger.getLogger(FacadeSessionBean.class);

   // Business methods

   /**
    * @ejb.interface-method
    */
   public void testSimple()
   {
      try
      {
         ChildLocalHome ch = ChildUtil.getLocalHome();
         ChildLocal child = ch.create(new Long(1), "Avoka", new Long(1));
         child.setSimpleParentId(new Long(2));
      }
      catch(Exception e)
      {
         throw new EJBException(e);
      }
   }

   /**
    * @ejb.interface-method
    */
   public void testComplex()
   {
      try
      {
         ChildLocalHome ch = ChildUtil.getLocalHome();
         ChildLocal child = ch.create(new Long(10), "Avoka", new Long(10));
         child.setComplexParentId1(new Long(2));
         child.setComplexParentId2(new Long(3));
      }
      catch(Exception e)
      {
         throw new EJBException(e);
      }
   }

   // SessionBean implementation

   /**
    * @exception  CreateException Description of Exception
    * @ejb.create-method
    */
   public void ejbCreate() throws CreateException {}
   public void ejbActivate() {}
   public void ejbPassivate() {}
   public void ejbRemove() {}

   public void setSessionContext(SessionContext ctx) {}
}
