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
package org.jboss.test.web.ejb;

import java.security.Principal;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.test.web.interfaces.InternalEntityHome;
import org.jboss.test.web.interfaces.InternalEntity;

/** A simple session bean for testing declarative security.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class EntityFacadeBean implements SessionBean
{
   static Logger log = Logger.getLogger(EntityFacadeBean.class);

   private SessionContext sessionContext;

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

   public void setSessionContext(SessionContext context)
   {
      sessionContext = context;
   }

   public void write(int value, boolean create)
      throws RemoteException
   {
      log.info("write, value=" + value+", create="+create);
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("echo, callerPrincipal=" + p);
      boolean isInternalUser = sessionContext.isCallerInRole("InternalUser");
      log.debug("Caller isInternalUser: "+isInternalUser);
      try
      {
         InitialContext ctx = new InitialContext();
         InternalEntityHome home = (InternalEntityHome) ctx.lookup("java:comp/env/ejb/InternalEntity");
         InternalEntity bean;
         if( create == true )
         {
            bean = home.create(value, value);
         }
         else
         {
            Integer pk = new Integer(value);
            bean = home.findByPrimaryKey(pk);
         }
         bean.setValue(value);
         if( create == false )
         {
            bean.remove();
         }
      }
      catch(Exception e)
      {
         throw new RemoteException("Failed to access InternalEntity", e);
      }
   }
}
