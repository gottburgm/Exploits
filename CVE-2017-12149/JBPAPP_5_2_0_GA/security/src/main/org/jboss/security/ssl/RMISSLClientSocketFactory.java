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
import java.rmi.server.RMIClientSocketFactory;
import java.security.cert.Certificate;
import java.util.Hashtable;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;

import org.jboss.logging.Logger;

/** An implementation of RMIClientSocketFactory that uses the JSSE
 default SSLSocketFactory to create a client SSLSocket.
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class RMISSLClientSocketFactory implements HandshakeCompletedListener,
   RMIClientSocketFactory, Serializable
{
   public static final String HANDSHAKE_COMPLETE_LISTENER =
      "org.jboss.security.ssl.HandshakeCompletedListener";
   private static Logger log = Logger.getLogger(RMISSLClientSocketFactory.class);
   private static final long serialVersionUID = -6412485012870705607L;
   private boolean wantsClientAuth = true;
   private boolean needsClientAuth = false;

   /** Creates new RMISSLClientSocketFactory */
   public RMISSLClientSocketFactory()
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
   * @param host - the host name
   * @param port - the port number
   * @return a socket connected to the specified host and port.
   * @exception IOException if an I/O error occurs during socket creation.
   */
   public java.net.Socket createSocket(String host, int port)
      throws IOException
   {
      SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
      socket.addHandshakeCompletedListener(this);
      socket.setWantClientAuth(wantsClientAuth);
      socket.setNeedClientAuth(needsClientAuth);
      log.debug("createSocket, host="+host+", port="+port
         +",needsClientAuth="+needsClientAuth+", wantsClientAuth="+wantsClientAuth);
      return socket;
   }

   public boolean equals(Object obj)
   {
      return obj instanceof RMISSLClientSocketFactory;
   }
   public int hashCode()
   {
      return getClass().getName().hashCode();
   }

   public void handshakeCompleted(HandshakeCompletedEvent event)
   {
      String cipher = event.getCipherSuite();
      SSLSession session = event.getSession();
      String peerHost = session.getPeerHost();
      Certificate[] localCerts = event.getLocalCertificates();
      Certificate[] peerCerts = null;
      try
      {
         peerCerts = event.getPeerCertificates();
      }
      catch(Exception e)
      {
         log.debug("Failed to retrieve peer certs", e);
      }
      log.debug("SSL handshakeCompleted, cipher="+cipher
         +", peerHost="+peerHost);
      int count = localCerts != null ? localCerts.length : 0;
      log.debug("ClientCertChain length: "+count);
      for(int n = 0; n < count; n ++)
         log.debug("Cert["+n+"]="+localCerts[n]);
      count = peerCerts != null ? peerCerts.length : 0;
      log.debug("PeerCertChain length: "+count);
      for(int n = 0; n < count; n ++)
         log.debug("Cert["+n+"]="+peerCerts[n]);
   
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
         log.debug("Failed to foward handshakeCompleted", e);
      }
   }
}