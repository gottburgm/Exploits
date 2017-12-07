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
package org.jboss.test.security.interceptors;

import java.security.Principal;
import java.util.Arrays;
import javax.naming.InitialContext;
import javax.management.MBeanInfo;

import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.server.MBeanInvoker;
import org.jboss.mx.server.Invocation;
import org.jboss.logging.Logger;
import org.jboss.security.srp.SRPSessionKey;
import org.jboss.security.srp.SRPServerSession;
import org.jboss.security.srp.jaas.SRPPrincipal;
import org.jboss.util.CachePolicy;

/** An interceptor that validates that the calling context has a valid SRP session
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SRPCacheInterceptor
   extends AbstractInterceptor
{
   private static Logger log = Logger.getLogger(SRPCacheInterceptor.class);
   private String cacheJndiName;

   public SRPCacheInterceptor()
   {
      super("SRPCacheInterceptor");
   }

   public void setAuthenticationCacheJndiName(String cacheJndiName)
   {
      this.cacheJndiName = cacheJndiName;
   }

   // Interceptor overrides -----------------------------------------
   public Object invoke(Invocation invocation) throws Throwable
   {
      String opName = invocation.getName();
      log.info("invoke, opName=" + opName);
      if( opName == null || opName.equals("testSession") == false )
      {
         Object value = invocation.nextInterceptor().invoke(invocation);
         return value;
      }

      Object[] args = invocation.getArgs();
      Principal userPrincipal = (Principal) args[0];
      String username = userPrincipal.getName();
      byte[] clientChallenge = (byte[]) args[1];

      try
      {
         InitialContext iniCtx = new InitialContext();
         CachePolicy cache = (CachePolicy) iniCtx.lookup(cacheJndiName);
         SRPSessionKey key;
         if (userPrincipal instanceof SRPPrincipal)
         {
            SRPPrincipal srpPrincpal = (SRPPrincipal) userPrincipal;
            key = new SRPSessionKey(username, srpPrincpal.getSessionID());
         }
         else
         {
            key = new SRPSessionKey(username);
         }
         Object cacheCredential = cache.get(key);
         if (cacheCredential == null)
         {
            throw new SecurityException("No SRP session found for: " + key);
         }
         log.debug("Found SRP cache credential: " + cacheCredential);
         /** The cache object should be the SRPServerSession object used in the
          authentication of the client.
          */
         if (cacheCredential instanceof SRPServerSession)
         {
            SRPServerSession session = (SRPServerSession) cacheCredential;
            byte[] challenge = session.getClientResponse();
            boolean isValid = Arrays.equals(challenge, clientChallenge);
            if ( isValid == false )
               throw new SecurityException("Failed to validate SRP session key for: " + key);
         }
         else
         {
            throw new SecurityException("Unknown type of cache credential: " + cacheCredential.getClass());
         }
         log.debug("Validated SRP cache credential for: "+key);
      }
      catch (Exception e)
      {
         log.error("Invocation failed", e);
         throw e;
      }

      Object value = invocation.nextInterceptor().invoke(invocation);
      return value;
   }
}
