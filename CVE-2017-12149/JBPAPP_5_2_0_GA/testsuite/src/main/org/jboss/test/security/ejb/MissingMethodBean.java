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

import org.jboss.logging.Logger;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import java.security.Principal;

/** A simple session bean that is used to test the 

 @author Scott.Stark@jboss.org
 @version $Revision: 81084 $
 */
public class MissingMethodBean implements SessionBean
{
   private static Logger log = Logger.getLogger(MissingMethodBean.class);
   private SessionContext sessionContext;

   public void ejbCreate() throws CreateException
   {
      MissingMethodBean.log.debug("ejbCreate() called");
   }

   public void ejbActivate()
   {
      MissingMethodBean.log.debug("ejbActivate() called");
   }

   public void ejbPassivate()
   {
      MissingMethodBean.log.debug("ejbPassivate() called");
   }

   public void ejbRemove()
   {
      MissingMethodBean.log.debug("ejbRemove() called");
   }

   public void setSessionContext(SessionContext context)
   {
      sessionContext = context;
   }

   public String invokeEcho(String arg)
   {
      log.debug("invokeEcho, arg="+arg);
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("echo, callerPrincipal="+p);
      boolean isCaller = sessionContext.isCallerInRole("EchoCaller");
      log.debug("echo, isCallerInRole('EchoCaller')="+isCaller);
      isCaller = sessionContext.isCallerInRole("InternalRole");
      log.debug("echo, isCallerInRole('InternalRole')="+isCaller);
      return arg;
   }

   public void callEcho()
   {
      log.debug("callEcho");
   }

   public void noop()
   {
      log.debug("noop");
   }
}
