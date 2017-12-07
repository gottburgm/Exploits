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
package org.jboss.test.security.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.Provider;
import java.security.Security;
import java.util.StringTokenizer;

import javax.net.ssl.SSLSocketFactory;

import org.jboss.invocation.http.interfaces.Util;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.test.util.SecurityProviderUtil;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.vfs.VirtualFileURLConnection;

/** A test mbean service that reads input from an https url passed in
 to its readURL method.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class HttpsClient extends ServiceMBeanSupport implements HttpsClientMBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private boolean addedHttpsHandler;

   private boolean addedJSSEProvider;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public HttpsClient()
   {
   }

   public String getName()
   {
      return "HttpsClient";
   }

   /** Read the contents of the given URL and return it. */
   public String readURL(String urlString) throws IOException
   {
      try
      {
         String reply = internalReadURL(urlString);
         log.debug("readURL -> " + reply);
         return reply;
      }
      catch (Throwable e)
      {
         log.error("Failed to readURL", e);
         throw new IOException("Failed to readURL, ex=" + e.getMessage());
      }
   }

   private String internalReadURL(String urlString) throws Exception
   {
      log.debug("Creating URL from string: " + urlString);
      URL url = new URL(urlString);
      log.debug("Created URL object from string, protocol=" + url.getProtocol());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      /* Override the host verifier so we can use a test server cert with
       a hostname that may not match the https url hostname.
      */
      System.setProperty("org.jboss.security.ignoreHttpsHost", "true");
      Util.configureHttpsHostVerifier(conn);

      log.debug("Connecting to URL: " + url);
      byte[] buffer = new byte[1024];
      int length = conn.getContentLength();
      log.debug("ContentLength: " + length);
      InputStream is = conn.getInputStream();
      StringBuffer reply = new StringBuffer();
      while ((length = is.read(buffer)) > 0)
         reply.append(new String(buffer, 0, length));
      log.debug("Done, closing streams");
      is.close();
      return reply.toString();
   }

   // Public --------------------------------------------------------
   protected void startService() throws Exception
   {
      addedJSSEProvider = false;
      try
      {
         new URL("https://www.https.test");
      }
      catch (MalformedURLException e)
      {
         // Install the default JSSE security provider
         Provider provider = SecurityProviderUtil.getJSSEProvider();
         log.debug("Adding " + provider.getName());

         addedJSSEProvider = Security.addProvider(provider) != -1;
         if (addedJSSEProvider)
         {
            log.debug("Added " + provider.getName());
         }

         addedHttpsHandler = false;
         // Install the JSSE https handler if it has not already been added
         String protocolHandler = SecurityProviderUtil.getProtocolHandlerName();

         String handlers = System.getProperty("java.protocol.handler.pkgs");
         if (handlers == null || handlers.indexOf(protocolHandler) < 0)
         {
            handlers += "|" + protocolHandler;
            log.debug("Adding https handler to java.protocol.handler.pkgs");
            System.setProperty("java.protocol.handler.pkgs", handlers);
            addedHttpsHandler = true;
         }
      }

      // Install the trust store
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL keyStoreURL = loader.getResource("META-INF/tst.keystore");
      if (keyStoreURL == null)
         throw new IOException("Failed to find resource tst.keystore");
      if (keyStoreURL.getProtocol().equals("vfszip"))
      {
         VirtualFileURLConnection conn = (VirtualFileURLConnection) keyStoreURL.openConnection();
         VirtualFile vf = conn.getContent();
         InputStream is = vf.openStream();
         File tmp = File.createTempFile("tst-", ".keystore");
         tmp.deleteOnExit();
         FileOutputStream fos = new FileOutputStream(tmp);
         byte[] buffer = new byte[1024];
         int bytes;
         while ((bytes = is.read(buffer)) > 0)
            fos.write(buffer, 0, bytes);
         fos.close();
         is.close();
         keyStoreURL = tmp.toURL();
      }
      log.debug("Setting javax.net.ssl.trustStore to: " + keyStoreURL.getPath());
      System.setProperty("javax.net.ssl.trustStore", keyStoreURL.getPath());
   }

   protected void stopService() throws Exception
   {
      if (addedJSSEProvider)
      {
         Provider provider = SecurityProviderUtil.getJSSEProvider();
         String name = provider.getName();
         log.debug("Removing " + name);
         Security.removeProvider(name);
      }

      if (addedHttpsHandler == true)
      {
         log.debug("Removing https handler from java.protocol.handler.pkgs");
         String protocolHandler = SecurityProviderUtil.getProtocolHandlerName();
         String handlers = System.getProperty("java.protocol.handler.pkgs");
         StringTokenizer tokenizer = new StringTokenizer(handlers, "|");
         StringBuffer buffer = new StringBuffer();
         while (tokenizer.hasMoreTokens())
         {
            String handler = tokenizer.nextToken();
            if (handler.equals(protocolHandler) == false)
            {
               buffer.append('|');
               buffer.append(handler);
            }
         }
         System.setProperty("java.protocol.handler.pkgs", buffer.toString());
      }
   }

   /** A SSLSocketFactory that logs the createSocket calls.
    */
   class DebugSSLSocketFactory extends SSLSocketFactory
   {
      SSLSocketFactory factoryDelegate;

      Logger theLog;

      DebugSSLSocketFactory(SSLSocketFactory factoryDelegate, Logger theLog)
      {
         this.factoryDelegate = factoryDelegate;
         this.theLog = theLog;
      }

      public Socket createSocket(java.net.InetAddress host, int port) throws java.io.IOException
      {
         theLog.debug("createSocket, host=" + host + ", port=" + port);
         Socket s = factoryDelegate.createSocket(host, port);
         theLog.debug("created socket=" + s);
         return s;
      }

      public Socket createSocket(String host, int port) throws java.io.IOException, java.net.UnknownHostException
      {
         theLog.debug("createSocket, host=" + host + ", port=" + port);
         Socket s = factoryDelegate.createSocket(host, port);
         theLog.debug("created socket=" + s);
         return s;
      }

      public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws java.io.IOException
      {
         theLog.debug("createSocket, socket=" + socket + ", host=" + host + ", port=" + port);
         Socket s = factoryDelegate.createSocket(socket, host, port, autoClose);
         theLog.debug("created socket=" + s);
         return s;
      }

      public Socket createSocket(java.net.InetAddress host, int port, java.net.InetAddress clientAddress, int clientPort)
            throws java.io.IOException
      {
         theLog.debug("createSocket, host=" + host + ", port=" + port + ", clientAddress=" + clientAddress
               + ", clientPort=" + clientPort);
         Socket s = factoryDelegate.createSocket(host, port, clientAddress, clientPort);
         theLog.debug("created socket=" + s);
         return s;
      }

      public Socket createSocket(String host, int port, java.net.InetAddress clientAddress, int clientPort)
            throws java.io.IOException, java.net.UnknownHostException
      {
         theLog.debug("createSocket, host=" + host + ", port=" + port + ", addr=" + clientAddress);
         Socket s = factoryDelegate.createSocket(host, port, clientAddress, clientPort);
         theLog.debug("created socket=" + s);
         return s;
      }

      public String[] getDefaultCipherSuites()
      {
         return factoryDelegate.getDefaultCipherSuites();
      }

      public String[] getSupportedCipherSuites()
      {
         return factoryDelegate.getSupportedCipherSuites();
      }
   }

}
