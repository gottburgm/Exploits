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
import java.util.Iterator;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.test.security.interfaces.SecurityContext;
import org.jboss.test.security.interfaces.SecurityContextHome;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SecurityContextBean implements SessionBean
{
   static Logger log = Logger.getLogger(SecurityContextBean.class);

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

   public void testDomainInteraction(Set expectedRoles)
   {
      // Validate that caller has the expected roles
      validateRoles(expectedRoles, true);
      // Access a bean from another security-domain
      try
      {
         InitialContext ctx = new InitialContext();
         SecurityContextHome home = (SecurityContextHome)ctx.lookup("java:comp/env/ejb/CalledBean");
         SecurityContext bean = home.create();
         SecurityContext thisBean = (SecurityContext) sessionContext.getEJBObject();
         bean.nestedInteraction(thisBean, expectedRoles);
      }
      catch(Exception e)
      {
         SecurityException se = new SecurityException("DataSource connection failed");
         se.initCause(e);
         throw se;         
      }
      // Validate that caller still has the expected roles
      validateRoles(expectedRoles, true);
   }

   public void nestedInteraction(SecurityContext caller, Set expectedRoles)
      throws RemoteException
   {
      validateRoles(expectedRoles, false);
   }

   /**
    * Validate that the current caller has every role from expectedRoles in the
    * context isCallerInRole set.
    * 
    * @param expectedRoles - Set<String> of the role names
    * @param isCallerInRoleFlag - Should isCallerInRole return true
    * @throws SecurityException - thrown if sessionContext.isCallerInRole(name)
    *    fails for any name in expectedRoles
    */ 
   private void validateRoles(Set expectedRoles, boolean isCallerInRoleFlag)
      throws SecurityException
   {
      Iterator names = expectedRoles.iterator();
      while( names.hasNext() )
      {
         String name = (String) names.next();
         boolean hasRole = sessionContext.isCallerInRole(name);
         if( hasRole != isCallerInRoleFlag )
            throw new SecurityException("Caller does not have role: "+name);
      }      
   }
}
