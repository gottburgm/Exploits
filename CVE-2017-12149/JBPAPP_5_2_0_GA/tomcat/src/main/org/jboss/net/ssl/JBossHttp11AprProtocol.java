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
package org.jboss.net.ssl;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.naming.InitialContext;

import org.apache.coyote.http11.Http11AprProtocol;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityUtil;
import org.jboss.security.plugins.JaasSecurityDomain;

/**
 * <p>
 * Extend {@link Http11AprProtocol} to allow encryption of the SSLPassword attribute.
 * The connector must have a securityDomain attribute with the JNDI name of the
 * {@link JaasSecurityDomain} that will decode the password.
 * 
 * @author <a href="mmoyses@redhat.com">Marcus Moyses</a>
 * @version $Revision: 1 $
 */
public class JBossHttp11AprProtocol extends Http11AprProtocol
{
   private String securityDomain;
   
   private String SSLPassword;
   
   private static final Logger log = Logger.getLogger(JBossHttp11AprProtocol.class);

   public void setSSLPassword(String SSLPassword)
   {
      this.SSLPassword = SSLPassword;
      if (this.securityDomain != null)
      {
         char[] password;
         try
         {
            if(log.isTraceEnabled())
               log.trace("Decoding password with security domain: " + this.securityDomain);
            password = DecodeAction.decode(this.SSLPassword, this.securityDomain);
         }
         catch (Exception e)
         {
            log.error("Error decoding password", e);
            throw new IllegalArgumentException("Error decoding password", e);
         }
         super.setSSLPassword(new String(password));
         password = null;
      }
   }

   public String getSecurityDomain()
   {
      return this.securityDomain;
   }

   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = SecurityUtil.unprefixSecurityDomain(securityDomain);
      if (this.SSLPassword != null)
         setSSLPassword(this.SSLPassword);
   }

   @SuppressWarnings("unchecked")
   private static class DecodeAction implements PrivilegedExceptionAction
   {
      String password;

      String jaasSecurityDomain;

      DecodeAction(String password, String jaasSecurityDomain)
      {
         this.password = password;
         this.jaasSecurityDomain = SecurityConstants.JAAS_CONTEXT_ROOT + "/" + jaasSecurityDomain;
      }

      public Object run() throws Exception
      {
         // Invoke the JaasSecurityDomain.decodeb64 operation
         InitialContext ctx = new InitialContext();
         JaasSecurityDomain securityDomain = (JaasSecurityDomain) ctx.lookup(jaasSecurityDomain);
         byte[] secret = securityDomain.decode64(password);
         // Convert to UTF-8 base char array
         String secretPassword = new String(secret, "UTF-8");
         return secretPassword.toCharArray();
      }

      static char[] decode(String password, String jaasSecurityDomain) throws Exception
      {
         DecodeAction action = new DecodeAction(password, jaasSecurityDomain);
         try
         {
            char[] decode = (char[]) AccessController.doPrivileged(action);
            return decode;
         }
         catch (PrivilegedActionException e)
         {
            throw e.getException();
         }
      }
   }
}