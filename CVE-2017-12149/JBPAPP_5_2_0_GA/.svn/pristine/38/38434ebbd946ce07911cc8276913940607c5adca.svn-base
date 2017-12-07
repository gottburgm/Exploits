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
package org.jboss.test.webservice.jbws309;

import org.jboss.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.rmi.RemoteException;

/**
 * An example of an EJB service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 26-Apr-2004
 */
public class OrganizationImpl implements SessionBean
{
   // provide logging
   private static final Logger log = Logger.getLogger(OrganizationImpl.class);

   public String getContactInfo(String organization) throws RemoteException
   {
      log.info("getContactInfo: " + organization);
      return "The '" + organization + "' boss is currently out of office, please call again.";
   }

   // SLSB Lifecycle ***************************************************************************************************

   public void ejbCreate()
   {
   }

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }
}
