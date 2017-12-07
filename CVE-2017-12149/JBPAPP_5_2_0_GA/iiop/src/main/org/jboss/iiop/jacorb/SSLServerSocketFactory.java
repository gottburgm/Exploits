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
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocket;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jboss.iiop.CorbaORBService;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityDomain;
import org.jboss.security.ssl.DomainServerSocketFactory;
import org.jboss.system.Registry;

/**
 * This implementation of the JacORB-specific interface 
 * <code>org.jacorb.orb.factory.ServerSocketFactory</code> uses the JSSE
 * KeyManagerFactory and TrustManagerFactory objects encapsulated by 
 * a JBossSX SecurityDomain. It looks up the 
 * <code>org.jboss.security.SecurityDomain</code> instance bound to the 
 * name <code>CorbaORBService.SSL_DOMAIN</code> in the JBoss registry.
 *
 * @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 91795 $
 */
public class SSLServerSocketFactory implements org.jacorb.orb.factory.ServerSocketFactory, Configurable
{
   // Static --------------------------------------------------------

   private static Logger log = Logger.getLogger(SSLServerSocketFactory.class);

   // Attributes ----------------------------------------------------

   private DomainServerSocketFactory domainFactory = null;

   private boolean require_mutual_auth = false;

   private boolean request_mutual_auth = false;

   // Constructor ---------------------------------------------------

   public SSLServerSocketFactory(org.jacorb.orb.ORB orb) throws IOException
   {
      log.info("Creating");

      SecurityDomain securityDomain = (SecurityDomain) Registry.lookup(CorbaORBService.SSL_DOMAIN);

      try
      {
         domainFactory = new DomainServerSocketFactory(securityDomain);
      }
      catch (IOException e)
      {
         log.warn("Could not create DomainServerSocketFactory: " + e);
         log.debug("Exception creating DomainServerSockedFactory: ", e);
         throw e;
      }

      short serverSupportedOptions = Short.parseShort(orb.getConfiguration().getAttribute(
            "jacorb.security.ssl.server.supported_options", "20"), 16); // 16 is the base as we take the string value as hex!      
      short serverRequiredOptions = Short.parseShort(orb.getConfiguration().getAttribute(
            "jacorb.security.ssl.server.required_options", "0"), 16); // 16 is the base as we take the string value as hex!      

      if ((serverSupportedOptions & 0x40) != 0)
      {
         // would prefer to establish trust in client.  If client can support
         // authentication, it will, otherwise we will continue
         request_mutual_auth = true;
      }
      if ((serverRequiredOptions & 0x40) != 0)
      {
         //required: establish trust in client
         //--> force other side to authenticate
         require_mutual_auth = true;
         request_mutual_auth = false;
      }
      if (request_mutual_auth)
         log.info("Will create SSL sockets that support client authentication");
      else if (require_mutual_auth)
         log.info("Will create SSL sockets that require client authentication");
      log.info("Created");
   }

   // JacORB ServerSocketFactory implementation ------------------
   // (interface org.jacorb.orb.factory.ServerSocketFactory)

   public ServerSocket createServerSocket(int port) throws IOException
   {
      SSLServerSocket s = (SSLServerSocket) domainFactory.createServerSocket(port);

      if (request_mutual_auth)
         s.setWantClientAuth(request_mutual_auth);
      else if (require_mutual_auth)
         s.setNeedClientAuth(require_mutual_auth);

      return s;
   }

   public ServerSocket createServerSocket(int port, int backlog) throws IOException
   {
      SSLServerSocket s = (SSLServerSocket) domainFactory.createServerSocket(port, backlog);

      if (request_mutual_auth)
         s.setWantClientAuth(request_mutual_auth);
      else if (require_mutual_auth)
         s.setNeedClientAuth(require_mutual_auth);

      return s;
   }

   public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException
   {
      SSLServerSocket s = (SSLServerSocket) domainFactory.createServerSocket(port, backlog, ifAddress);

      if (request_mutual_auth)
         s.setWantClientAuth(request_mutual_auth);
      else if (require_mutual_auth)
         s.setNeedClientAuth(require_mutual_auth);

      return s;
   }

   // Avalon Configurable implementation ----------------------------

   public void configure(Configuration configuration) throws ConfigurationException
   {
      // no-op
   }
}
