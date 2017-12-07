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
package org.jboss.test.testbyvalue.bean;

import java.rmi.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.Context;

/**
 * $Id: ByValueStatelessSessionBean.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 * @author Clebert Suconic
 */
public class ByValueStatelessSessionBean implements SessionBean
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   private SessionContext sessionContext;

   public void ejbCreate() throws RemoteException, CreateException
   {
   }

   public void ejbActivate() throws RemoteException
   {
   }

   public void ejbPassivate() throws RemoteException
   {
   }

   public void ejbRemove() throws RemoteException
   {
   }

   public void setSessionContext(SessionContext context) throws RemoteException
   {
      sessionContext = context;
      //Exception e = new Exception("in set Session context");
      //log.debug("failed", e);
   }

    public void doTestByValue(ClassWithProperty propertyClass) throws RemoteException
    {
        propertyClass.setX(propertyClass.getX() + 1000);
    }
}
