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
package org.jboss.test.lock.bean;

import java.rmi.*;
import javax.ejb.*;

import org.jboss.test.lock.interfaces.EnterpriseEntityHome;
import org.jboss.test.lock.interfaces.EnterpriseEntity;

public class EnterpriseEntityBean
   implements EntityBean
{
   static org.jboss.logging.Logger log =
      org.jboss.logging.Logger.getLogger(EnterpriseEntityBean.class);

   private EntityContext entityContext;

   public String name;
   public String field;
   public EnterpriseEntity nextEntity;
   public String lastEntity = "UNKNOWN!!!!";

   public String ejbCreate(final String name)
      throws RemoteException, CreateException
   {
      this.name = name;
      return null;
   }

   public void ejbPostCreate(String name)
      throws RemoteException, CreateException
   {
      // empty
   }

   public void ejbActivate() throws RemoteException
   {
      // empty
   }

   public void ejbLoad() throws RemoteException
   {
      // empty
   }

   public void ejbPassivate() throws RemoteException
   {
      // empty
   }

   public void ejbRemove() throws RemoteException, RemoveException
   {
      // empty
   }

   public void ejbStore() throws RemoteException
   {
      // empty
   }

   public void setField(String field) throws RemoteException
   {
      //log.debug("Bean "+name+", setField("+field+") called");
      this.field = field;
   }

   public String getField() throws RemoteException
   {
      return field;
   }

   public void setAndCopyField(String field) throws RemoteException
   {
      //log.debug("Bean "+name+", setAndCopyField("+field+") called");
		
      log.debug("setAndCopyField");
      setField(field);
      if (nextEntity == null)
      {
         log.error("nextEntity is null!!!!!!!!, lastEntity: " + lastEntity);
      }
      nextEntity.setField(field);
   }

   public void setNextEntity(String beanName) throws RemoteException
   {
      try
      {
         log.debug("setNextEntity: " + beanName);
         EJBObject ejbObject = entityContext.getEJBObject();
         EnterpriseEntityHome home = (EnterpriseEntityHome) ejbObject.getEJBHome();
         try
         {
            nextEntity = home.findByPrimaryKey(beanName);
         }
         catch (FinderException e)
         {
            nextEntity = home.create(beanName);
         }
         lastEntity = beanName;
      }
      catch (Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException
            ("create entity did not work check messages");
      }
   }

   public void setEntityContext(EntityContext context)
      throws RemoteException
   {
      entityContext = context;
   }

   public void unsetEntityContext() throws RemoteException
   {
      entityContext = null;
   }

   public void sleep(long time) throws InterruptedException
   {
      Thread.sleep(time);
   }
}
