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
package org.jboss.test.security.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jboss.logging.Logger;

//$Id: UsefulStatelessSessionBean.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  Stateless Session Bean testing declarative/programmatic security
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 20, 2006
 *  @version $Revision: 81036 $
 */
public class UsefulStatelessSessionBean implements SessionBean
{ 
   /** The serialVersionUID */
   private static final long serialVersionUID = -6735401489611989066L;
   static Logger log = Logger.getLogger(UsefulStatelessSessionBean.class); 
   private SessionContext sessionContext;
   
   public void setSessionContext(SessionContext ctx) 
   throws EJBException, RemoteException
   {
      this.sessionContext = ctx; 
   }

   public void ejbCreate() throws CreateException
   {
      log.debug("ejbCreate() called");
   }

   public void ejbActivate()
   {
      log.debug("ejbActivate() called");
   }

   public void ejbPassivate()
   {
      log.debug("ejbPassivate() called");
   }

   public void ejbRemove()
   {
      log.debug("ejbRemove() called");
   }
   
   //Check isCallerInRole
   public boolean isCallerInRole(String rolename)
   {
      return this.sessionContext.isCallerInRole(rolename);
   } 
}
