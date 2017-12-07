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
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
 

/** An implementation of RMIClientSocketFactory that uses the JCE Cipher
 with an SRP session key to create an encrypted stream.

@author  Scott.Stark@jboss.org
@version $Revision: 85945 $
*/
public class CipherClientSocketFactory implements RMIClientSocketFactory, Serializable
{
   private static final long serialVersionUID = -6412485012870705607L;

   /** Creates new CipherClientSocketFactory */
   public CipherClientSocketFactory()
   {
   }

   /** Create a client socket connected to the specified host and port.
   * @param host - the host name
   * @param port - the port number
   * @return a socket connected to the specified host and port.
   * @exception IOException if an I/O error occurs during socket creation.
   */
   public Socket createSocket(String host, int port)
      throws IOException
   {
      CipherSocket socket = null;
      return socket;
   }

   public boolean equals(Object obj)
   {
      return obj instanceof CipherClientSocketFactory;
   }
   public int hashCode()
   {
      return getClass().getName().hashCode();
   }

}
