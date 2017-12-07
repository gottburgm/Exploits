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
package org.jboss.test.cts.ejb;

import java.rmi.RemoteException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;


import org.jboss.test.cts.keys.AccountPK;
import org.jboss.logging.Logger;

/**
 * A simple cmp2 entity bean implementation
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public abstract class CtsCmpBean
   implements EntityBean
{
   static Logger log = Logger.getLogger(CtsCmpBean.class);
   private EntityContext ctx = null;

   public AccountPK ejbCreate(AccountPK pk, String personsName)
      throws CreateException, DuplicateKeyException
   {
      log.debug("entry ejbCreate, pk=" + pk);
      setPk(pk);
      setPersonsName(personsName);
      return null;
   }

   public void ejbPostCreate(AccountPK pk, String personsName)
      throws CreateException, DuplicateKeyException, EJBException,
      RemoteException
   {
      log.debug("entry ejbPostCreate, pk=" + pk);
   }

   public void ejbLoad()
      throws EJBException, RemoteException
   {
      log.debug("ejbLoad () called");

   }

   public void ejbStore()
      throws EJBException, RemoteException
   {
      log.debug("ejbStore () called");

   }

   public void ejbRemove()
      throws EJBException, RemoteException
   {
      log.debug("ejbRemove () called");

   }

   public void ejbActivate()
      throws EJBException, RemoteException
   {
      log.debug("ejbActivate () called");
   }

   public void ejbPassivate()
      throws EJBException, RemoteException
   {
      log.debug("ejbPassivate () called");
   }

   public void setEntityContext(EntityContext ctx)
      throws EJBException, RemoteException
   {
      log.debug("setEntityContext called");
      this.ctx = ctx;
   }

   public void unsetEntityContext()
      throws EJBException, RemoteException
   {
      log.debug("unsetEntityContext () called");

      ctx = null;
   }

   public abstract void setPk(AccountPK pk);
   public abstract AccountPK getPk();

   public abstract void setPersonsName(String personsName);
   public abstract String getPersonsName();

   public abstract void setPersonsAge(int age);
   public abstract int getPersonsAge();

}
