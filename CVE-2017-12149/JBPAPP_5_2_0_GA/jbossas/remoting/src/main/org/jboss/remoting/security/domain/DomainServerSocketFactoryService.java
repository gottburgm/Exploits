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
package org.jboss.remoting.security.domain;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.naming.InitialContext;
import org.jboss.security.SecurityDomain;
import org.jboss.security.ssl.DomainServerSocketFactory;

/**
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public class DomainServerSocketFactoryService implements DomainServerSocketFactoryServiceMBean
{
   private String securityDomain = null;
   private DomainServerSocketFactory serverSocketFactory = null;

   /**
    * Returns an unbound server socket. The socket is configured with the socket
    * options (such as accept timeout) given to this factory.
    *
    * @return
    * @throws java.io.IOException
    */
   public ServerSocket createServerSocket() throws IOException
   {
      return serverSocketFactory.createServerSocket();
   }

   /**
    * Returns a server socket bound to the specified port. The socket is configured
    * with the socket options (such as accept timeout) given to this factory.
    *
    * @param i
    * @return
    * @throws java.io.IOException
    */
   public ServerSocket createServerSocket(int i) throws IOException
   {
      return serverSocketFactory.createServerSocket(i);
   }

   /**
    * Returns a server socket bound to the specified port,
    * and uses the specified connection backlog. The socket is configured
    * with the socket options (such as accept timeout) given to this factory.
    *
    * @param i
    * @param i1
    * @return
    * @throws java.io.IOException
    */
   public ServerSocket createServerSocket(int i, int i1) throws IOException
   {
      return serverSocketFactory.createServerSocket(i, i1);
   }

   /**
    * Returns a server socket bound to the specified port, with a specified
    * listen backlog and local IP. The bindAddr argument can be used on a multi-homed
    * host for a ServerSocket that will only accept connect requests to one of its addresses.
    * The socket is configured with the socket options (such as accept timeout) given to this factory.
    *
    * @param i
    * @param i1
    * @param inetAddress
    * @return
    * @throws java.io.IOException
    */
   public ServerSocket createServerSocket(int i, int i1, InetAddress inetAddress) throws IOException
   {
      return serverSocketFactory.createServerSocket(i, i1, inetAddress);
   }

   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   public String getSecurityDomain()
   {
      return securityDomain;
   }

   /**
    * start the service, create is already called
    */
   public void start() throws Exception
   {
      if(securityDomain != null)
      {
         InitialContext ctx = new InitialContext();
         SecurityDomain domain = (SecurityDomain) ctx.lookup(securityDomain);
         serverSocketFactory = new DomainServerSocketFactory();
         serverSocketFactory.setSecurityDomain(domain);
      }
      else
      {
         throw new Exception("Can not create server socket factory due to the SecurityDomain not being set.");
      }
   }

   /**
    * create the service, do expensive operations etc
    */
   public void create() throws Exception
   {
      //NOOP
   }

   /**
    * stop the service
    */
   public void stop()
   {
      //NOOP
   }

   /**
    * destroy the service, tear down
    */
   public void destroy()
   {
      //NOOP
   }

}