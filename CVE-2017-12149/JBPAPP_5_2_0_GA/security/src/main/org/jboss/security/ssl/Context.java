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
package org.jboss.security.ssl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jboss.logging.Logger;
import org.jboss.security.SecurityDomain;
import org.jboss.security.plugins.JaasSecurityDomain;

/** 
 * Utility class with a static method that returns an initialized JSSE 
 * SSLContext for a given JBossSX SecurityDomain.
 *
 * @see javax.net.ssl.KeyManagerFactory
 * @see javax.net.ssl.SSLContext
 * @see javax.net.ssl.TrustManager
 * @see javax.net.ssl.TrustManagerFactory
 * @see org.jboss.security.SecurityDomain
 * 
 * @author  Scott.Stark@jboss.org
 * @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 *
 * @version $Revision: 110302 $
 */
class Context
{
   private static Logger log = Logger.getLogger(Context.class);

   /*
    * Returns an initialized JSSE SSLContext that uses the KeyManagerFactory
    * and TrustManagerFactory objects encapsulated by a given JBossSX 
    * SecurityDomain.
    */
   static SSLContext forDomain(SecurityDomain securityDomain)
      throws IOException
   {
      SSLContext sslCtx = null;
      try
      {
         sslCtx = SSLContext.getInstance("TLS");
         KeyManagerFactory keyMgr = securityDomain.getKeyManagerFactory();
         if( keyMgr == null )
            throw new IOException("KeyManagerFactory is null for security domain: "+securityDomain.getSecurityDomain());
         KeyManager[] keyMgrs = keyMgr.getKeyManagers();
         if (securityDomain instanceof JaasSecurityDomain)
            keyMgrs = ((JaasSecurityDomain) securityDomain).getKeyManagers();
         TrustManagerFactory trustMgr = securityDomain.getTrustManagerFactory();
         TrustManager[] trustMgrs = null;
         if( trustMgr != null )
            trustMgrs = trustMgr.getTrustManagers();
         sslCtx.init(keyMgrs, trustMgrs, null);
         return sslCtx;
      }
      catch(NoSuchAlgorithmException e)
      {
         log.error("Failed to get SSLContext for TLS algorithm", e);
         throw new IOException("Failed to get SSLContext for TLS algorithm");
      }
      catch(KeyManagementException e)
      {
         log.error("Failed to init SSLContext", e);
         throw new IOException("Failed to init SSLContext");
      }
      catch(SecurityException e)
      {
         log.error("Failed to init SSLContext", e);
         throw new IOException("Failed to init SSLContext");
      }
   }
}
