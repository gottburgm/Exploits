/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.iiop.jacorb;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jboss.iiop.CorbaORBService;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityDomain;
import org.jboss.security.ssl.DomainSocketFactory;
import org.jboss.system.Registry;

/**
 * This implementation of the JacORB-specific interface 
 * <code>org.jacorb.orb.factory.SocketFactory</code> uses the JSSE
 * KeyManagerFactory and TrustManagerFactory objects encapsulated by 
 * a JBossSX SecurityDomain. It looks up the 
 * <code>org.jboss.security.SecurityDomain</code> instance bound to the 
 * name <code>CorbaORBService.SSL_DOMAIN</code> in the JBoss registry.
 *
 * @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 91795 $
 */
public class SSLSocketFactory implements org.jacorb.orb.factory.SocketFactory, Configurable
{
   // Static --------------------------------------------------------

   private static Logger log = Logger.getLogger(SSLSocketFactory.class);

   // Attributes ----------------------------------------------------

   private DomainSocketFactory domainFactory = null;

   // Constructor ---------------------------------------------------

   public SSLSocketFactory(org.jacorb.orb.ORB orb) throws IOException
   {
      log.info("Creating");

      SecurityDomain securityDomain = (SecurityDomain) Registry.lookup(CorbaORBService.SSL_DOMAIN);

      try
      {
         domainFactory = new DomainSocketFactory(securityDomain);
      }
      catch (IOException e)
      {
         log.warn("Could not create DomainSocketFactory: " + e);
         log.debug("Exception creating DomainSockedFactory: ", e);
         throw e;
      }
   }

   // JacORB SSLSocketFactory implementation ------------------------
   // (interface org.jacorb.orb.factory.SSLSocketFactory)

   /**
    * create a connected stream Socket.
    *
    * @param host the host name
    * @param port the port number
    * @return a connected stream Socket
    * @throws IOException, UnknownHostException
    */
   public Socket createSocket(String host, int port) throws IOException, UnknownHostException
   {
      return domainFactory.createSocket(host, port);
   }

   /**
    * create a connected stream Socket.
    *
    * @param host the host name
    * @param port the port number
    * @param timeout the timeout value to be used in milliseconds
    * @return a connected stream Socket
    * @throws IOException
    */
   public Socket createSocket(String host, int port, int timeout) throws IOException, UnknownHostException
   {
      return domainFactory.createSocket(host, port, timeout);
   }

   public boolean isSSL(java.net.Socket s)
   {
      return (s instanceof SSLSocket);
   }

   // Avalon Configurable implementation ----------------------------

   public void configure(Configuration configuration) throws ConfigurationException
   {
      // no-op
   }

}
