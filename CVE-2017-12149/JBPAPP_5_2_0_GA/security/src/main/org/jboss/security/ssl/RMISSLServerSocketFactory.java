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
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.server.RMIServerSocketFactory;

import org.jboss.security.SecurityDomain;

/** An implementation of RMIServerSocketFactory that uses a
 DomainServerSocketFactory for its implementation. This class is just an
 adaptor from the RMIServerSocketFactory to the DomainServerSocketFactory.

 This class is not suitable for RMI object that require a Serializable socket
 factory like activatable services. The reason for this limitation is that
 a SecurityDomain is not serializable due to its association with a local
 KeyStore.

@author Scott.Stark@jboss.org
@version $Revision: 85945 $
*/
public class RMISSLServerSocketFactory implements RMIServerSocketFactory
{
   private DomainServerSocketFactory domainFactory;

   /** Creates new RMISSLServerSocketFactory initialized with a
    DomainServerSocketFactory with not security domain. The setSecurityDomain
    method must be invoked to establish the correct non-default value.
    */
   public RMISSLServerSocketFactory()
   {
      domainFactory = new DomainServerSocketFactory();
   }

   public String getBindAddress()
   {
      return domainFactory.getBindAddress();
   }
   public void setBindAddress(String host) throws UnknownHostException
   {
      domainFactory.setBindAddress(host);
   }

   public SecurityDomain getSecurityDomain()
   {
      return domainFactory.getSecurityDomain();
   }
   public void setSecurityDomain(SecurityDomain securityDomain)
   {
      domainFactory.setSecurityDomain(securityDomain);
   }

   public boolean isWantsClientAuth()
   {
      return domainFactory.isWantsClientAuth();
   }
   public void setWantsClientAuth(boolean wantsClientAuth)
   {
      domainFactory.setWantsClientAuth(wantsClientAuth);
   }

   public boolean isNeedsClientAuth()
   {
      return domainFactory.isNeedsClientAuth();
   }
   public void setNeedsClientAuth(boolean needsClientAuth)
   {
      domainFactory.setNeedsClientAuth(needsClientAuth);
   }
   public String[] getCiperSuites()
   {
      return domainFactory.getCiperSuites();
   }
   public void setCiperSuites(String[] ciperSuites)
   {
      domainFactory.setCiperSuites(ciperSuites);
   }

   public String[] getProtocols()
   {
      return domainFactory.getProtocols();
   }
   public void setProtocols(String[] protocols)
   {
      domainFactory.setProtocols(protocols);
   }

   /**
    * Create a server socket on the specified port (port 0 indicates
    * an anonymous port).
    * @param  port the port number
    * @return the server socket on the specified port
    * @exception IOException if an I/O error occurs during server socket
    * creation
    */
   public ServerSocket createServerSocket(int port)
      throws IOException
   {
      return domainFactory.createServerSocket(port);
   }

   public boolean equals(Object obj)
   {
      return obj instanceof RMISSLServerSocketFactory;
   }
   public int hashCode()
   {
      return getClass().getName().hashCode();
   }
}
