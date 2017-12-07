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
package org.jboss.jmx.connector.invoker;

import java.security.Principal;

import javax.naming.InitialContext;
import javax.security.auth.Subject;

import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.mx.server.Invocation;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.SubjectSecurityManager;

/** A security interceptor that requires an authorized user for invoke(Invocation)
 * operation calls when the SecurityDomain and SecurityMgr attributes are
 * specified. Access to attributes and the MBeanInfo are not intercepted.
 *
 * @see Interceptor
 *
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 104060 $
 *   
 */
public final class AuthenticationInterceptor extends AbstractInterceptor
{
   private SubjectSecurityManager securityMgr;

   private String securityDomain;

   private boolean initialized = false;

   public void setSecurityDomain(String securityDomain) throws Exception
   {
      this.securityDomain = securityDomain;
   }

   /**
    * 
    * @param invocation
    * @return
    * @throws Throwable
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
	  SecurityContext previousSC = null;
      String type = invocation.getType();
      Subject subject = null;
      if (!initialized)
         initialize();
      if (type == Invocation.OP_INVOKE && securityMgr != null)
      {
         String opName = invocation.getName();
         if (opName.equals("invoke"))
         {
            Object[] args = invocation.getArgs();
            org.jboss.invocation.Invocation inv = (org.jboss.invocation.Invocation) args[0];
            // Authenticate the caller based on the security association
            Principal caller = inv.getPrincipal();
            Object credential = inv.getCredential();
            subject = new Subject();
            boolean isValid = securityMgr.isValid(caller, credential, subject);
            if (isValid == false)
            {
               String msg = "Failed to authenticate principal=" + caller + ", securityDomain="
                     + securityMgr.getSecurityDomain();
               throw new SecurityException(msg);

            }
            String securityDomain = SecurityConstants.DEFAULT_APPLICATION_POLICY;
            if (securityMgr != null)
               securityDomain = securityMgr.getSecurityDomain();
            // store current security context
            previousSC = SecurityActions.getSecurityContext();
            SecurityContext sc = SecurityActions.createSecurityContext(securityDomain);
            SecurityActions.setSecurityContext(sc);
            // Push the caller security context
            SecurityActions.pushSubjectContext(caller, credential, subject);
         }
      }

      try
      {
         Interceptor i = invocation.nextInterceptor();
         return i.invoke(invocation);
      }
      finally
      {
         // restore previous security context
         if (subject != null)
            SecurityActions.setSecurityContext(previousSC);
      }
   }

   private void initialize()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         securityMgr = (SubjectSecurityManager) ctx.lookup(securityDomain);
      }
      catch (Exception e)
      {

      }
      if (securityMgr == null)
         log.warn("Unable to locate security domain " + securityDomain
               + ". The AuthenticationInterceptor will have no effect");
      initialized = true;
   }
}
