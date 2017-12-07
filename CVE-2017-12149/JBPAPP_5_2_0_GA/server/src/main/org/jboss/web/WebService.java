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
package org.jboss.web;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.bootstrap.spi.util.ServerConfigUtil;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.threadpool.BasicThreadPoolMBean;

/**
 * The WebService implementation. It configures a WebServer instance to
 * perform dynamic class and resource loading.
 *
 * @jmx:mbean
 *      extends="org.jboss.system.ServiceMBean"
 *      name="jboss:service=WebService"
 *
 * @version <tt>$Revision: 80636 $</tt>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 */
public class WebService extends ServiceMBeanSupport
   implements WebServiceMBean
{
   //
   // jason: WebService and WebServer classes should be merged into one
   //        WebService only provides a thin layer of JMX which WebServer
   //        can do just as well.
   //
   //        Doing so will dramatically simplify this component.
   //
   //        Rename to HTTPClassLoaderService to be clear that this is not
   //        a fully functional web server.
   //

   /**
    * The web server instance which does the work.
    * <p>
    * Note: This value shadows the MBeanServer server value
    * from ServiceMBeanSupport.
    */
   private WebServer server = new WebServer();

   /** The host portion of the RMI codebase URL */
   private String host;
   
   /**
    * @jmx:managed-constructor
    */
   public WebService()
   {
      super();
   }

   /**
    * @jmx:managed-operation
    */
   public URL addClassLoader(ClassLoader cl)
   {
      return server.addClassLoader(cl);
   }

   /**
    * @jmx:managed-operation
    */
   public void removeClassLoader(ClassLoader cl)
   {
      server.removeClassLoader(cl);
   }

   /**
    * Set the WebService listening port.
    * 
    * @jmx:managed-attribute
    *
    * @param port  The listening port; 0 for anonymous
    */
   public void setPort(int port)
   {
      server.setPort(port);
   }

   /**
    * Get the WebService listening port.
    * 
    * @jmx:managed-attribute
    */
   public int getPort()
   {
      return server.getPort();
   }

   /**
    * Set the name of the interface to use for the host portion of the
    * RMI codebase URL.
    * 
    * @jmx:managed-attribute
    */
   public void setHost(final String hostname)
   {
      this.host = ServerConfigUtil.fixRemoteAddress(hostname);
   }

   /**
    * Get the name of the interface to use for the host portion of the
    * RMI codebase URL.
    * 
    * @jmx:managed-attribute
    */
   public String getHost()
   {
      return host;
   }

   /**
    * Set the specific address the WebService listens on.  This can be used on
    * a multi-homed host for a ServerSocket that will only accept connect requests
    * to one of its addresses.
    *
    * @jmx:managed-attribute
    * 
    * @param address  the interface name or IP address to bind. If host is null,
    *                 connections on any/all local addresses will be allowed.
    */
   public void setBindAddress(String address) throws UnknownHostException
   {
      InetAddress bindAddress = toInetAddress(address);
      server.setBindAddress(bindAddress);
   }
   
   /**
    * Get the specific address the WebService listens on.
    *
    * @jmx:managed-attribute
    * 
    * @return the interface name or IP address the WebService binds to.
    */
   public String getBindAddress()
   {
      InetAddress bindAddress = server.getBindAddress();
      
      if (bindAddress != null)
         return bindAddress.getHostAddress();
      else
         return null;
   }

   /**
    * Get the WebService listen queue backlog limit. The maximum queue length
    * for incoming connection indications (a request to connect) is set to the
    * backlog parameter. If a connection indication arrives when the queue is
    * full, the connection is refused.
    *
    * @jmx:managed-attribute
    * 
    * @return the queue backlog limit. 
    */
   public int getBacklog()
   {
      return server.getBacklog();
   }

   /**
    * Set the WebService listen queue backlog limit. The maximum queue length
    * for incoming connection indications (a request to connect) is set to the
    * backlog parameter. If a connection indication arrives when the queue is
    * full, the connection is refused.
    *
    * @jmx:managed-attribute
    * 
    * @param backlog, the queue backlog limit. 
    */
   public void setBacklog(int backlog)
   {
      server.setBacklog(backlog);
   }

   /** Set the thread pool used for the WebServer class loading.
    * @jmx:managed-attribute
    * 
    */ 
   public void setThreadPool(BasicThreadPoolMBean threadPool)
   {
      server.setThreadPool(threadPool);
   }

   /**
    * A flag indicating if the server should attempt to download classes from
    * thread context class loader when a request arrives that does not have a
    * class loader key prefix.
    *
    * @jmx:managed-attribute
    */
   public boolean getDownloadServerClasses()
   {
      return server.getDownloadServerClasses();
   }

   /**
    * @jmx:managed-attribute
    */
   public void setDownloadServerClasses(boolean flag)
   {
      server.setDownloadServerClasses(flag);
   }

   /**
    * A flag indicating if the server should attempt to download resources,
    * i.e. resource paths that don't end in .class
    *
    * @jmx:managed-attribute
    */
   public boolean getDownloadResources()
   {
      return server.getDownloadResources();
   }

   /**
    * @jmx:managed-attribute
    */
   public void setDownloadResources(boolean flag)
   {
      server.setDownloadResources(flag);
   }

   /**
    * The RMI codebase URL.
    * 
    * @jmx:managed-attribute
    */
   public String getCodebase()
   {
      return System.getProperty("java.rmi.server.codebase");
   }
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }

   protected void createService() throws Exception
   {
      // Load the file mime.types into the mapping list
      Properties mimeTypes = new Properties();

      try
      {
         mimeTypes.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/jboss/web/mime.types"));

         Enumeration keys = mimeTypes.keys();
         while (keys.hasMoreElements())
         {
            String extension = (String) keys.nextElement();
            String type = mimeTypes.getProperty(extension);
            server.addMimeType(extension, type);
         }
      }
      catch (Exception e)
      {
         log.error("Failed to load org/jboss/web/mime.types; ignoring", e);
      }

      // if no override has been specified, default to the jboss bind address
      if (getBindAddress() == null)
         setBindAddress(System.getProperty(ServerConfig.SERVER_BIND_ADDRESS));
      
      // if no host specified, default to the java.rmi.server.hostname property value
      if (getHost() == null)
         setHost(System.getProperty("java.rmi.server.hostname"));
      
      // Set the rmi codebase if it is not already set
      String codebase = getCodebase();
      if (codebase == null)
      {
         codebase = "http://" + getHost() + ":" + getPort() + "/";
         System.setProperty("java.rmi.server.codebase", codebase);
      }
      log.info("Using RMI server codebase: " + codebase);
   }

   /**
    * Start the web server for dynamic downloading of classes and resources.
    * 
    * <p>
    * The system <tt>java.rmi.server.codebase</tt> is also set to
    * <em>http://<host>:<port>/</em> if the property has not been set.
    */
   protected void startService() throws Exception
   {
      // Start the WebServer running
      server.start();
      log.debug("Started WebServer with address: " + server.getBindAddress() + ":" + getPort());
   }

   protected void stopService() throws Exception
   {
      server.stop();
      log.debug("Stopped WebServer with address: " + server.getBindAddress() + ":" + getPort());
   }
   
   /**
    * Safely convert a host string to InetAddress or null
    */
   private InetAddress toInetAddress(String host) throws UnknownHostException
   {
      if (host == null || host.length() == 0)
         return null;
      else
         return InetAddress.getByName(host);
   }
}
