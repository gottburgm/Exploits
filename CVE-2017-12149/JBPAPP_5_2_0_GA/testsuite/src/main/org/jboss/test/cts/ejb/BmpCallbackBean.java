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



import org.jboss.test.cts.keys.*;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.Collection;
import java.sql.*;
import javax.naming.*;
import javax.ejb.*;
import javax.sql.DataSource;


public class BmpCallbackBean
   implements EntityBean
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
    EntityContext ctx;

   /**
    * Method ejbCreate
    *
    *
    * @param pk
    * @param personsName
    *
    * @return
    *
    * @throws CreateException
    * @throws DuplicateKeyException
    * @throws EJBException
    * @throws RemoteException
    *
    */

   public AccountPK ejbCreate (AccountPK pk, String personsName)
      throws CreateException, DuplicateKeyException, EJBException,
             RemoteException
   {
      log.debug("entry ejbCreate");

      return new AccountPK(pk.getKey());
   }

   /**
    * Method ejbFindByPrimaryKey
    *
    *
    * @param pk
    *
    * @return
    *
    * @throws EJBException
    * @throws FinderException
    * @throws RemoteException
    *
    */

   public AccountPK ejbFindByPrimaryKey (AccountPK pk)
      throws FinderException, EJBException, RemoteException
   {
      log.debug("entry ejbFindByPrimaryKey");

      return new AccountPK(pk.getKey());
   }

   /**
    * Method ejbPostCreate
    *
    *
    * @param pk
    * @param personsName
    *
    * @throws CreateException
    * @throws DuplicateKeyException
    * @throws EJBException
    * @throws RemoteException
    *
    */

   public void ejbPostCreate (AccountPK pk, String personsName)
      throws CreateException, DuplicateKeyException, EJBException,
             RemoteException
   {
      log.debug("ejbPostCreate (AccountPK, String) called");
   }

   /**
    * Method ejbLoad
    *
    *
    * @throws EJBException
    * @throws RemoteException
    *
    */

   public void ejbLoad ()
      throws EJBException, RemoteException
   {
      log.debug("ejbLoad () called");
   }

   /**
    * Method ejbStore
    *
    *
    * @throws EJBException
    * @throws RemoteException
    *
    */

   public void ejbStore ()
      throws EJBException, RemoteException
   {
      log.debug("ejbStore () called");

   }

   /**
    * Method ejbRemove
    *
    *
    * @throws EJBException
    * @throws RemoteException
    *
    */

   public void ejbRemove ()
      throws EJBException, RemoteException
   {
      log.debug("ejbRemove () called");
   }

   /**
    * Method ejbActivate
    *
    *
    * @throws EJBException
    * @throws RemoteException
    *
    */

   public void ejbActivate ()
      throws EJBException, RemoteException
   {
      log.debug("ejbActivate () called");
   }

   /**
    * Method ejbPassivate
    *
    *
    * @throws EJBException
    * @throws RemoteException
    *
    */

   public void ejbPassivate ()
      throws EJBException, RemoteException
   {
      log.debug("ejbPassivate () called");
   }

   /**
    * Method setEntityContext
    *
    *
    * @param ctx
    *
    * @throws EJBException
    * @throws RemoteException
    *
    */

   public void setEntityContext (EntityContext ctx)
      throws EJBException, RemoteException
   {
      log.debug("setEntityContext () called");

      this.ctx = ctx;

   }

   /**
    * Method unsetEntityContext
    *
    *
    * @throws EJBException
    * @throws RemoteException
    *
    */

   public void unsetEntityContext ()
      throws EJBException, RemoteException
   {
      log.debug("unsetEntityContext () called");

      ctx = null;
   }

}


/*------ Formatted by Jindent 3.23 Basic 1.0 --- http://www.jindent.de ------*/


