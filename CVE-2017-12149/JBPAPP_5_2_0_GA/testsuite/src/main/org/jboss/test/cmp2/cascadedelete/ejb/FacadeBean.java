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
package org.jboss.test.cmp2.cascadedelete.ejb;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import java.rmi.RemoteException;
import java.util.Iterator;

import javax.ejb.EJBException;


/**
 * A FacadeBean.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 85945 $
 */
public class FacadeBean implements SessionBean
{
   public void createScenario1() throws RemoteException
   {
      try
      {
         InitialContext ic = new InitialContext();
         CustomerLocalHome ch = (CustomerLocalHome) ic.lookup("CustomerLocal");
         CustomerLocal customer = ch.create(new Long(1), "customer1");

         AccountLocalHome ah = (AccountLocalHome) ic.lookup("AccountLocal");
         AccountLocal acc22 = ah.create(new Long(22), "account22");
         acc22.setCustomer(customer);
         
         AccountLocal acc33 = ah.create(new Long(33), "account33");
         acc33.setParentAccount(acc22);
         acc33.setCustomer(customer);
      }
      catch (Exception e)
      {
         throw new EJBException("Failed to create scenario 1", e);
      }
   }

   
   public void createScenario2() throws RemoteException
   {
      try
      {
         InitialContext ic = new InitialContext();
         CustomerLocalHome ch = (CustomerLocalHome) ic.lookup("CustomerLocal");
         CustomerLocal customer = ch.create(new Long(1), "customer1");

         AccountLocalHome ah = (AccountLocalHome) ic.lookup("AccountLocal");
         AccountLocal acc11 = ah.create(new Long(11), "account11");
         acc11.setCustomer(customer);

         AccountLocal acc22 = ah.create(new Long(22), "account22");
         acc22.setCustomer(customer);
         acc11.setParentAccount(acc22);
         
         AccountLocal acc33 = ah.create(new Long(33), "account33");
         acc33.setParentAccount(acc22);
         acc33.setParentAccount2(acc11);
         acc33.setCustomer(customer);
      }
      catch (Exception e)
      {
         throw new EJBException("Failed to create scenario 2", e);
      }
   }

   public void deleteBeans() throws RemoteException
   {
      try
      {
         InitialContext ic = new InitialContext();
         CustomerLocalHome ch = (CustomerLocalHome) ic.lookup("CustomerLocal");         
         ch.remove(new Long(1));
      }
      catch(Exception e)
      {
         throw new EJBException(e);
      }
   }

   public void ejbCreate() throws CreateException
   {
   }

   public void setSessionContext(SessionContext ctx)
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }
}
