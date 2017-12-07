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
package org.jboss.test.bankiiop.ejb;

import java.io.ObjectStreamException;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

import org.jboss.test.bankiiop.interfaces.AccountData;
import org.jboss.test.bankiiop.interfaces.Customer;

/**
 *      
 *   @see <related>
 *   @author $Author: dimitris@jboss.org $
 *   @version $Revision: 81036 $
 */
public class AccountBeanCMP
   extends AccountBean
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   public String id;
   public float balance;
   public Customer owner;
   
   private boolean dirty;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public String getId()
   {
      return id;
   }
   
   public void setId(String id)
   {
      this.id = id;
      dirty = true;
   }
   
   public float getBalance()
   {
      return balance;
   }
   
   public void setBalance(float balance)
   {
      this.balance = balance;
      dirty = true;
   }
   
   public Customer getOwner()
   {
      return owner;
   }
   
   public void setOwner(Customer owner)
   {
      this.owner = owner;
      dirty = true;
   }
   
   public void setData(AccountData data)
   {
      setBalance(data.getBalance());
      setOwner(data.getOwner());
   }
   
   public AccountData getData()
   {
      AccountData data = new AccountData();
      data.setId(id);
      data.setBalance(balance);
      data.setOwner(owner);
      return data;
   }
   
   public boolean isModified()
   {
      return dirty;
   }
   
   // EntityBean implementation -------------------------------------
   public String ejbCreate(AccountData data) 
      throws RemoteException, CreateException
   { 
      setId(data.id);
      setData(data);
      dirty = false;
      return null;
   }
   
   public void ejbPostCreate(AccountData data) 
      throws RemoteException, CreateException
   { 
   }
   
   public void ejbLoad()
      throws RemoteException
   {
      super.ejbLoad();
      dirty = false;
   }
}

/*
 *   $Id: AccountBeanCMP.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 *   Currently locked by:$Locker$
 *   Revision:
 *   $Log$
 *   Revision 1.2  2005/10/29 23:41:18  starksm
 *   Update the jboss LGPL headers
 *
 *   Revision 1.1  2002/03/15 22:36:28  reverbel
 *   Initial version of the bank test for JBoss/IIOP.
 *
 *   Revision 1.2  2001/01/07 23:14:34  peter
 *   Trying to get JAAS to work within test suite.
 *
 *   Revision 1.1.1.1  2000/06/21 15:52:37  oberg
 *   Initial import of jBoss test. This module contains CTS tests, some simple examples, and small bean suites.
 *
 *
 *  
 */
