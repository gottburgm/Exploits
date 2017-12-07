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
package org.jboss.invocation.pooled.interfaces;

import java.io.Serializable;
import java.io.IOException;
import javax.net.SocketFactory;

/**
 * This class encapsulates all the required information for a client to 
 * establish a connection with the server.
 * 
 * It also attempts to provide a fast hash() function since this object
 * is used as a key in a hashmap mainted by the ConnectionManager. 
 *
 * @author Bill Burke
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public class ServerAddress implements Serializable
{
   /** The serialVersionUID @since 1.1.4.1 */
   private static final long serialVersionUID = -7206359745950445445L;

   /**
    * Address of host to connect to
    * @serial
    */
   public String address;

   /**
    * Port the service is listening on
    * @serial
    */
   public int port;

   /**
    * If the TcpNoDelay option should be used on the socket.
    * @serial
    */
   public boolean enableTcpNoDelay = false;

   /**
    * Timeout of setSoTimeout
    * @serial
    */
   public int timeout = 60000;
   /** An option socket factory for connecting to the server
    * @serial
    */
   public SocketFactory clientSocketFactory;

   /**
    * This object is used as a key in a hashmap,
    * so we precompute the hascode for faster lookups.
    */
   private transient int hashCode;

   /**
    * The server address/port representation.
    * 
    * @param address - hostname/ip of the server
    * @param port - the invoker port
    * @param enableTcpNoDelay - the Socket.setTcpNoDelay flag
    * @param timeout - the Socket.setSoTimeout value
    * @param clientSocketFactory - optional SocketFactory
    */ 
   public ServerAddress(String address, int port, boolean enableTcpNoDelay,
      int timeout, SocketFactory clientSocketFactory)
   {
      this.address = address;
      this.port = port;
      this.enableTcpNoDelay = enableTcpNoDelay;
      this.hashCode = address.hashCode() + port;
      if( enableTcpNoDelay )
         this.hashCode ++;
      this.timeout = timeout;
      this.clientSocketFactory = clientSocketFactory;
   }

   public String toString()
   {
      return "[address:" + address + ",port:" + port + ",enableTcpNoDelay:" + enableTcpNoDelay + "]";
   }

   public boolean equals(Object obj)
   {
      try
      {
         // Compare this to obj
         ServerAddress o = (ServerAddress) obj;
         if (port != o.port)
            return false;
         if (address.equals(o.address) == false)
            return false;
         if (enableTcpNoDelay != o.enableTcpNoDelay)
            return false;
         return true;
      }
      catch (Throwable e)
      {
         return false;
      }
   }

   public int hashCode()
   {
      return hashCode;
   }

   /**
    * Create the transient hashCode 
    * @param in
    * @throws IOException
    * @throws ClassNotFoundException
    */
   private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
   {
      // Trigger default serialization
      in.defaultReadObject();
      // Build the hashCode
      this.hashCode = address.hashCode() + port;
      if( enableTcpNoDelay )
         this.hashCode ++;
   }
}
