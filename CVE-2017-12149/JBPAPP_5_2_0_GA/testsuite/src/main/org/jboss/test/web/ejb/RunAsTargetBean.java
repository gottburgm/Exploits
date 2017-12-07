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
import java.util.StringTokenizer;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/** A simple session bean for testing declarative security.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class RunAsTargetBean implements SessionBean
{
   static Logger log = Logger.getLogger(RunAsTargetBean.class);

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

   /**
    * Validate the run-as principal and roles
    */ 
   public void checkRunAs()
   {
      Principal caller = sessionContext.getCallerPrincipal();
      String callerName = caller.getName();
      log.debug("checkRunAs, caller="+caller);
      try
      {
         // Check the expected principal name
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         String name = (String) enc.lookup("runAsName");
         if( name.equals(callerName) == false )
            throw new EJBException("runAsName mismatch, "+name+"!="+callerName);
         // Check the expected roles
         String roles = (String) enc.lookup("runAsRoles");
         StringTokenizer st = new StringTokenizer(roles, ",");
         while( st.hasMoreTokens() )
         {
            String role = st.nextToken();
            boolean inRole = sessionContext.isCallerInRole(role);
            String msg = "isCallerInRole("+role+"): "+inRole;
            log.debug(msg);
            if( inRole == false )
               throw new EJBException("Failed check: "+msg);
         }
      }
      catch(NamingException e)
      {
         throw new EJBException("Failed to access enc", e);
      }
   }

}
