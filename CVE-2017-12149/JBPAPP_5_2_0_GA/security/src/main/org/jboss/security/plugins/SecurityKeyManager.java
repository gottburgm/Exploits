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
package org.jboss.security.plugins;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

/**
 * X509KeyManager that allows selection of a key entry to be used.
 * 
 * @author <a href="mmoyses@redhat.com">Marcus Moyses</a>
 * @version $Revision: 1.1 $
 */
public class SecurityKeyManager implements X509KeyManager
{
   private X509KeyManager delegate;
   
   private String serverAlias;
   
   private String clientAlias;
   
   public SecurityKeyManager(X509KeyManager keyManager, String serverAlias, String clientAlias)
   {
      this.delegate = keyManager;
      this.serverAlias = serverAlias;
      this.clientAlias  = clientAlias;
   }

   /**
    * @see X509KeyManager#chooseClientAlias(String[], Principal[], Socket)
    */
   public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket)
   {
      if (clientAlias != null)
         return clientAlias;
      return delegate.chooseClientAlias(keyType, issuers, socket);
   }

   /**
    * @see X509KeyManager#chooseServerAlias(String, Principal[], Socket)
    */
   public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket)
   {
      if (serverAlias != null)
         return serverAlias;
      return delegate.chooseServerAlias(keyType, issuers, socket);
   }

   /**
    * @see X509KeyManager#getCertificateChain(String)
    */
   public X509Certificate[] getCertificateChain(String alias)
   {
      return delegate.getCertificateChain(alias);
   }

   /**
    * @see X509KeyManager#getClientAliases(String, Principal[])
    */
   public String[] getClientAliases(String keyType, Principal[] issuers)
   {
      return delegate.getClientAliases(keyType, issuers);
   }

   /**
    * @see X509KeyManager#getPrivateKey(String)
    */
   public PrivateKey getPrivateKey(String alias)
   {
      return delegate.getPrivateKey(alias);
   }

   /**
    * @see X509KeyManager#getServerAliases(String, Principal[])
    */
   public String[] getServerAliases(String keyType, Principal[] issuers)
   {
      return delegate.getServerAliases(keyType, issuers);
   }

}
