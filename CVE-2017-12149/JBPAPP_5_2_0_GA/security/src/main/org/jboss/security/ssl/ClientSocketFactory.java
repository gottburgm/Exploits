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
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;

import org.jboss.logging.Logger;

/** An implementation of SocketFactory that uses the JSSE
 default SSLSocketFactory to create a client SSLSocket.
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ClientSocketFactory extends SocketFactory
   implements HandshakeCompletedListener, Serializable
{
   public static final String HANDSHAKE_COMPLETE_LISTENER =
      "org.jboss.security.ssl.HandshakeCompletedListener";
   static final long serialVersionUID = -2762336418317218104L;
   private static Logger log = Logger.getLogger(ClientSocketFactory.class);
   private boolean wantsClientAuth = true;
   private boolean needsClientAuth = false;

   /** Creates new ClientSocketFactory */
   public ClientSocketFactory()
   {
   }

   public boolean isWantsClientAuth()
   {
      return wantsClientAuth;
   }
   public void setWantsClientAuth(boolean wantsClientAuth)
   {
      this.wantsClientAuth = wantsClientAuth;
   }

   public boolean isNeedsClientAuth()
   {
      return needsClientAuth;
   }
   public void setNeedsClientAuth(boolean needsClientAuth)
   {
      this.needsClientAuth = needsClientAuth;
   }

   /** Create a client socket connected to the specified host and port.
   * @param serverHost - the host name
   * @param serverPort - the port number
   * @return a socket connected to the specified host and port.
   * @exception IOException if an I/O error occurs during socket creation.
   */
   public Socket createSocket(String serverHost, int serverPort)
      throws IOException, UnknownHostException
   {
      InetAddress serverAddr = InetAddress.getByName(serverHost);
      return this.createSocket(serverAddr, serverPort);
   }

   public Socket createSocket(String serverHost, int serverPort,
      InetAddress clientAddr, int clientPort)
      throws IOException, UnknownHostException
   {
      InetAddress serverAddr = InetAddress.getByName(serverHost);
      return this.createSocket(serverAddr, serverPort, clientAddr, clientPort);
   }
   public Socket createSocket(InetAddress serverAddr, int serverPort)
      throws IOException
   {
      return this.createSocket(serverAddr, serverPort, null, 0);
   }
   public Socket createSocket(InetAddress serverAddr, int serverPort,
      InetAddress clientAddr, int clientPort)
      throws IOException
   {
      SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      SSLSocket socket = (SSLSocket) factory.createSocket(serverAddr, serverPort, clientAddr, clientPort);
      socket.addHandshakeCompletedListener(this);
      socket.setNeedClientAuth(needsClientAuth);
      socket.setWantClientAuth(wantsClientAuth);
      return socket;
   }

   public boolean equals(Object obj)
   {
      return obj instanceof ClientSocketFactory;
   }
   public int hashCode()
   {
      return getClass().getName().hashCode();
   }

   public void handshakeCompleted(HandshakeCompletedEvent event)
   {
      if( log.isTraceEnabled() )
      {
         String cipher = event.getCipherSuite();
         SSLSession session = event.getSession();
         String peerHost = session.getPeerHost();
         log.debug("SSL handshakeCompleted, cipher="+cipher
            +", peerHost="+peerHost);
      }

      /* See if there is a HANDSHAKE_COMPLETE_LISTENER. This is not done from
      within a priviledged action as access to the SSL session through the
      callback is not considered an implementation detail.
      */
      try
      {
         Hashtable env = System.getProperties();
         HandshakeCompletedListener listener =
            (HandshakeCompletedListener) env.get(HANDSHAKE_COMPLETE_LISTENER);
         if( listener != null )
            listener.handshakeCompleted(event);
      }
      catch(Throwable e)
      {
         log.debug("Failed to forward handshakeCompleted", e);
      }
   }

}
