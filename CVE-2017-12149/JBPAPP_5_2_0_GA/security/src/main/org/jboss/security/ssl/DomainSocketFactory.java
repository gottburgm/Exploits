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
package org.jboss.security.ssl;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Hashtable;
import javax.naming.InitialContext;
import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.jboss.logging.Logger;
import org.jboss.security.SecurityDomain;

/**
 * An implementation of SocketFactory that creates SSL sockets using the 
 * JSSE SSLContext and a JBossSX SecurityDomain for the KeyManagerFactory 
 * and TrustManagerFactory objects.
 *
 * @see javax.net.ssl.SSLContext
 * @see org.jboss.security.SecurityDomain
 *
 * @author  Scott.Stark@jboss.org
 * @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 *
 * @version $Revision: 102452 $
 */
public class DomainSocketFactory 
   extends SSLSocketFactory
   implements HandshakeCompletedListener, Serializable
{
   public static final String HANDSHAKE_COMPLETE_LISTENER =
      "org.jboss.security.ssl.HandshakeCompletedListener";
   /** @since 1.5.4.5 (4.0.4) */
   private static final long serialVersionUID = -4471907598525153511L;
   private static Logger log = Logger.getLogger(DomainSocketFactory.class);
   private transient SecurityDomain securityDomain;
   private transient SSLContext sslCtx = null;
   private boolean wantsClientAuth = true;
   private boolean needsClientAuth = false;

   /** 
    * A default constructor for use when created by Class.newInstance. The
    * factory is not usable until its SecurityDomain has been established.
    */
   public DomainSocketFactory()
   {
   }

   /** 
    * Create a socket factory instance that uses the given SecurityDomain
    * as the source for the SSL KeyManagerFactory and TrustManagerFactory.
    */
   public DomainSocketFactory(SecurityDomain securityDomain) 
      throws IOException
   {
      if( securityDomain == null )
         throw new IOException("The securityDomain may not be null");
      this.securityDomain = securityDomain;
   }

   public SecurityDomain getSecurityDomain()
   {
      return securityDomain;
   }

   public void setSecurityDomain(SecurityDomain securityDomain)
   {
      this.securityDomain = securityDomain;
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

   // SSLSocketFactory methods --------------------------------------

   /** 
    * Create a client socket connected to the specified host and port.
    * 
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

   /** 
    * Create a client socket connected to the specified host and port.
    * 
    * @param serverHost - the host name
    * @param serverPort - the port number
    * @param timeout the timeout value to be used in milliseconds
    * @return a socket connected to the specified host and port.
    * @exception IOException if an I/O error occurs during socket creation.
    */
   public Socket createSocket(String serverHost, int serverPort, int timeout)
      throws IOException, UnknownHostException
   {
      InetAddress serverAddr = InetAddress.getByName(serverHost);
      return this.createSocket(serverAddr, serverPort, timeout);
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
      initSSLContext();
      SSLSocketFactory factory = sslCtx.getSocketFactory();
      SSLSocket socket = 
         (SSLSocket)factory.createSocket(serverAddr, serverPort, 
                                         clientAddr, clientPort);
      String[] supportedProtocols = socket.getSupportedProtocols();
      log.debug("Supported protocols: " + Arrays.asList(supportedProtocols));
      String[] protocols = supportedProtocols; // {"SSLv3"};
      socket.setEnabledProtocols(protocols);
      socket.addHandshakeCompletedListener(this);
      socket.setNeedClientAuth(needsClientAuth);
      socket.setWantClientAuth(wantsClientAuth);
      return socket;
   }

   public Socket createSocket(InetAddress serverAddr, 
                              int serverPort, int timeout)
      throws IOException
   {
      initSSLContext();
      SSLSocketFactory factory = sslCtx.getSocketFactory();
      SSLSocket socket = (SSLSocket)factory.createSocket();
      socket.connect(new InetSocketAddress(serverAddr, serverPort), timeout);
      String[] supportedProtocols = socket.getSupportedProtocols();
      log.debug("Supported protocols: " + Arrays.asList(supportedProtocols));
      String[] protocols = supportedProtocols; // {"SSLv3"};
      socket.setEnabledProtocols(protocols);
      socket.addHandshakeCompletedListener(this);
      socket.setNeedClientAuth(needsClientAuth);
      socket.setWantClientAuth(wantsClientAuth);
      return socket;
   }

   public Socket createSocket(Socket s, String host, 
                              int port, boolean autoClose) 
      throws IOException
   {
      initSSLContext();
      SSLSocketFactory factory = sslCtx.getSocketFactory();
      SSLSocket socket = 
         (SSLSocket)factory.createSocket(s, host, port, autoClose);
      String[] supportedProtocols = socket.getSupportedProtocols();
      String[] protocols = supportedProtocols; // {"SSLv3"};
      socket.setEnabledProtocols(protocols);
      socket.addHandshakeCompletedListener(this);
      socket.setNeedClientAuth(needsClientAuth);
      socket.setWantClientAuth(wantsClientAuth);
      return socket;
   }
   
   public Socket createSocket() throws IOException
   {
	   initSSLContext();
	   SSLSocketFactory factory = sslCtx.getSocketFactory();
	   SSLSocket socket = (SSLSocket) factory.createSocket();
	   String[] supportedProtocols = socket.getSupportedProtocols();
	   String[] protocols = supportedProtocols; // {"SSLv3"};
	   socket.setEnabledProtocols(protocols);
	   socket.addHandshakeCompletedListener(this);
	   socket.setNeedClientAuth(needsClientAuth);
	   socket.setWantClientAuth(wantsClientAuth);
	   return socket;
   }

   public String[] getDefaultCipherSuites()
   {
      String[] cipherSuites = {};
      try
      {
         initSSLContext();
         SSLSocketFactory factory = sslCtx.getSocketFactory();
         cipherSuites = factory.getDefaultCipherSuites();
      }
      catch(IOException e)
      {
         log.error("Failed to get default SSLSocketFactory", e);
      }      
      return cipherSuites;
   }
   
   public String[] getSupportedCipherSuites()
   {
      String[] cipherSuites = {};
      try
      {
         initSSLContext();
         SSLSocketFactory factory = sslCtx.getSocketFactory();
         cipherSuites = factory.getSupportedCipherSuites();
      }
      catch(IOException e)
      {
         log.error("Failed to get default SSLSocketFactory", e);
      }      
      return cipherSuites;
   }
   
   /** 
    * The default SocketFactory which looks to the java:/jaas/other
    * security domain configuration.
    */
   public static SocketFactory getDefault()
   {
      DomainSocketFactory ssf = null;
      try
      {
         InitialContext iniCtx = new InitialContext();
         SecurityDomain sd = (SecurityDomain)iniCtx.lookup("java:/jaas/other");
         ssf = new DomainSocketFactory(sd);
      }
      catch(Exception e)
      {
         log.error("Failed to create default SocketFactory", e);
      }
      return ssf;
   }
   
   // HandshakeCompletedListener method -----------------------------

   public void handshakeCompleted(HandshakeCompletedEvent event)
   {
      Logger log = Logger.getLogger(ClientSocketFactory.class);
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
         log.debug("Failed to foward handshakeCompleted", e);
      }
   }

   // Private method ------------------------------------------------

   private void initSSLContext()
      throws IOException
   {
      if( sslCtx != null )
         return;
      sslCtx = Context.forDomain(securityDomain);
   }

}
