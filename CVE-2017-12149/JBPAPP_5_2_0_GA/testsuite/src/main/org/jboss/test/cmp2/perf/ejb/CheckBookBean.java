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
package org.jboss.test.cmp2.perf.ejb;

import java.util.Collection;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public abstract class CheckBookBean implements EntityBean
{
   public CheckBookBean()
   {
   }

   public void ejbActivate() throws EJBException
   {
   }

   public void ejbLoad() throws EJBException
   {
   }

   public void ejbPassivate() throws EJBException
   {
   }

   public void ejbRemove() throws RemoveException, EJBException
   {
   }

   public void ejbStore() throws EJBException
   {
   }

   public void setEntityContext(EntityContext ctx) throws EJBException
   {
   }

   public void unsetEntityContext() throws EJBException
   {
   }

   public void remove()
      throws RemoveException, EJBException
   {
   }

   public String ejbCreate(String account, double balance) throws CreateException
   {
      this.setAccount(account);
      this.setBalance(balance);
      return null;
   }
   public void ejbPostCreate(String account, double balance) throws CreateException
   {

   }

   public abstract Collection getCheckBookEntries();
   public abstract void setCheckBookEntries(Collection entries);

   public abstract String getAccount();
   public abstract void setAccount(String account);
   public abstract double getBalance();
   public abstract void setBalance(double balance);
}
