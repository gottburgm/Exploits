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

import java.security.Principal;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.jboss.logging.Logger;
import org.jboss.test.security.interfaces.CalledSessionLocalHome;
import org.jboss.test.security.interfaces.CalledSessionLocal;

/**
 * A simple session bean that calls the CalleeBean
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class CallerFacadeBean implements SessionBean
{
   private static Logger log = Logger.getLogger(CallerFacadeBean.class);
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
    * This method calls echo on a StatelessSessionLocal and asserts that the
    * caller is in the EchoCaller role.
    */
   public String invokeEcho(String arg)
   {
      log.debug("echo, arg=" + arg);
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("echo, callerPrincipal=" + p);
      boolean isEchoCaller = sessionContext.isCallerInRole("EchoCaller");
      log.debug("echo, isCallerInRole('EchoCaller')=" + isEchoCaller);
      boolean isInternalRole = sessionContext.isCallerInRole("InternalRole");
      log.debug("echo, isCallerInRole('InternalRole')=" + isInternalRole);

      try
      {
         InitialContext ic = new InitialContext();
         Context enc = (Context) ic.lookup("java:comp/env");
         Object ref = enc.lookup("ejb/CalledSessionLocalHome");
         CalledSessionLocalHome home = (CalledSessionLocalHome) PortableRemoteObject.narrow(ref,
            CalledSessionLocalHome.class);
         CalledSessionLocal localBean = home.create();
         String echo2 = localBean.invokeEcho(arg);
         log.debug("echo#1, callee.invokeEcho=" + echo2);
         echo2 = localBean.invokeEcho(arg);
         log.debug("echo#2, callee.invokeEcho=" + echo2);
      }
      catch (Exception e)
      {
         log.error("Failed to invoke CalledSession.invokeEcho", e);
         throw new EJBException("Failed to invoke Callee.echo", e);
      }
      return arg;
   }

   /**
    * This method should call invokeEcho on another CalledSession
    */
   public void callEcho()
   {
      try
      {
         InitialContext ic = new InitialContext();
         Context enc = (Context) ic.lookup("java:comp/env");
         Object ref = enc.lookup("ejb/CalledSessionLocalHome");
         CalledSessionLocalHome home = (CalledSessionLocalHome) PortableRemoteObject.narrow(ref,
            CalledSessionLocalHome.class);
         CalledSessionLocal bean = home.create();
         bean.callLocalEcho("callEcho#2");
         log.debug("echo, callee.callLocalEcho#1");
         bean.callLocalEcho("callEcho#2");
         log.debug("echo, callee.callLocalEcho#2");
      }
      catch (Exception e)
      {
         log.error("Failed to invoke Callee.invokeEcho", e);
         throw new EJBException("Failed to invoke Callee.invokeEcho", e);
      }

   }

   public void noop()
   {
      log.debug("noop");
   }

}
