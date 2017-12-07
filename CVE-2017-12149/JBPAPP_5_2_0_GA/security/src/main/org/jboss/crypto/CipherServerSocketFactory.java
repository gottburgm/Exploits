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
package org.jboss.crypto;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.server.RMIServerSocketFactory;


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
public class CipherServerSocketFactory implements RMIServerSocketFactory
{

   /** Creates new RMISSLServerSocketFactory */
   public CipherServerSocketFactory()
   {
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
      CipherServerSocket socket = null;
      return socket;
   }

   public boolean equals(Object obj)
   {
      return obj instanceof CipherServerSocketFactory;
   }
   public int hashCode()
   {
      return getClass().getName().hashCode();
   }
}
