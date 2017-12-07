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
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jboss.logging.Logger;
import org.jboss.test.web.interfaces.ReferenceTest;
import org.jboss.test.web.interfaces.ReturnData;

/** A simple session bean for testing declarative security.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class StatelessSessionBean implements SessionBean
{
   static Logger log = Logger.getLogger(StatelessSessionBean.class);

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

   public String echo(String arg)
   {
      log.debug("echo, arg=" + arg);
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("echo, callerPrincipal=" + p);
      return p.getName();
   }

   public String forward(String echoArg)
   {
      log.debug("StatelessSessionBean2.forward, echoArg=" + echoArg);
      return echo(echoArg);
   }

   public void noop(ReferenceTest test, boolean optimized)
   {
      log.debug("noop");
   }

   public ReturnData getData()
   {
      ReturnData data = new ReturnData();
      data.data = "TheReturnData";
      return data;
   }

   /** A method deployed with no method permissions */
   public void unchecked()
   {
      log.debug("unchecked");      
   }

   /** A method deployed with method permissions such that only a run-as
    * assignment will allow access. 
    */
   public void checkRunAs()
   {
      Principal caller = sessionContext.getCallerPrincipal();
      log.debug("checkRunAs, caller="+caller);
      boolean isInternalUser = sessionContext.isCallerInRole("InternalUser");
      log.debug("Caller isInternalUser: "+isInternalUser);
   }
}
