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

import java.io.IOException;
import java.security.KeyStore;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;

import org.jboss.security.SecurityDomain;
import org.jboss.security.plugins.JaasSecurityDomain;
import org.apache.tomcat.util.net.jsse.JSSESocketFactory;
import org.apache.tomcat.util.net.jsse.JSSEKeyManager;
import javax.net.ssl.X509KeyManager;

/**
 * Extends the tomcat JSSE14SocketFactory to obtain the server key and trust
 * stores from the SecurityDomain defined by the securityDomain attribute
 * of the connector.
 * 
 */ 
public class JBossSocketFactory
   extends JSSESocketFactory
{
   private SecurityDomain securityDomain;

   public JBossSocketFactory()
   {
   }

   public void setAttribute(String name, Object value)
   {
      if (name.equalsIgnoreCase("securityDomain"))
      {
         try
         {
            setSecurityDomainName((String) value);
         }
         catch (Exception e)
         {
            IllegalArgumentException ex =
               new IllegalArgumentException("Failed to set security domain");
            ex.initCause(e);
            throw ex;
         }
      }
      super.setAttribute(name, value);
   }

   /**
    * Set the SecurityDomain to use for the key/trust stores
    * 
    * @param jndiName - the jndi name of the SecurityDomain binding
    * @throws NamingException
    * @throws IOException
    */ 
   public void setSecurityDomainName(String jndiName)
      throws NamingException, IOException
   {
      InitialContext iniCtx = new InitialContext();
      securityDomain = (SecurityDomain) iniCtx.lookup(jndiName);
   }

   /**
    * Gets the SSL server's keystore from the SecurityDomain.
    * 
    * @param type - ignored, this comes from the security domain config
    * @param pass - ignore, this comes from the security domain config
    * @return the KeyStore for the server cert
    * @throws IOException
    */ 
   protected KeyStore getKeystore(String type, String pass)
      throws IOException
   {
      verifySecurityDomain();
      return securityDomain.getKeyStore();
   }

   /*
    * Gets the SSL server's truststore from the SecurityDomain.
    
    * @param type - ignored, this comes from the security domain config
    * @return the KeyStore for the trusted signers store
    */
   protected KeyStore getTrustStore(String type) throws IOException
   {
      verifySecurityDomain();
      return securityDomain.getTrustStore();
   }

   /**
    * Override to obtain the TrustManagers from the security domain.
    * 
    * @param keystoreType - ignored, this comes from the security domain
    * @param algorithm - ignored, this comes from the security domain
    * @return the array of TrustManagers from the security domain
    * @throws Exception
    */ 
   protected TrustManager[] getTrustManagers(String keystoreType, String algorithm)
      throws Exception
   {
      verifySecurityDomain();
      TrustManagerFactory tmf = securityDomain.getTrustManagerFactory();
      TrustManager[] trustMgrs = null;

      if( tmf != null )
      {
          trustMgrs = tmf.getTrustManagers();
      }
      return trustMgrs;
   }

   /**
    * Override to obtain the KeyManagers from the security domain.
    * 
    * @param keystoreType - ignored, this comes from the security domain
    * @param algorithm - ignored, this comes from the security domain
    * @param keyAlias - ignored
    * @return the array of KeyManagers from the security domain
    * @throws Exception
    */ 
   protected KeyManager[] getKeyManagers(String keystoreType, String algorithm,
      String keyAlias)
      throws Exception
   {
      verifySecurityDomain();
      KeyManagerFactory kmf = securityDomain.getKeyManagerFactory();
      KeyManager[] keyMgrs = null;
      if( kmf != null )
      {
         keyMgrs = kmf.getKeyManagers();
         if (securityDomain instanceof JaasSecurityDomain)
            keyMgrs = ((JaasSecurityDomain) securityDomain).getKeyManagers();
      }
      return keyMgrs;
   }

   @Override
   protected KeyManager[] getKeyManagers(String keystoreType, String keystoreProvider, String algorithm, String keyAlias)
         throws Exception
   {
      return getKeyManagers(keystoreType, algorithm, keyAlias);
   }
   
   

   @Override
   protected TrustManager[] getTrustManagers(String keystoreType, String keystoreProvider, String algorithm)
         throws Exception
   {
      return getTrustManagers(keystoreType, algorithm);
   }

   private void verifySecurityDomain()
   {
      String str = "securityDomain is null." +
            "Set it as an attribute in the connector setting";
      
      if(this.securityDomain == null)
         throw new IllegalStateException(str);
   }
}
