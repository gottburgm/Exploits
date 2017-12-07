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
package org.jboss.test.pooled.bean;

import java.rmi.RemoteException;
import java.security.Principal;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import org.jboss.logging.Logger;

/**
 * @author Scott.Stark@jboss.com
 * @version $Revision: 81036 $
 */
public class StatelessSessionBean implements SessionBean
{
   static Logger log = Logger.getLogger(StatelessSessionBean.class);

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
   }

   public void noop()
   {

   }

   public Principal echoCaller(String info) throws RemoteException
   {
      Principal caller = sessionContext.getCallerPrincipal();
      return caller;
   }

}
