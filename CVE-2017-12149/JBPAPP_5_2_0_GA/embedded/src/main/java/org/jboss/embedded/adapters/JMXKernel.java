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
package org.jboss.embedded.adapters;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Properties;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.kernel.Kernel;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.system.ServiceController;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.deployers.ServiceDeployer;
import org.jboss.system.server.ServerConfigImpl;
import org.jboss.system.server.ServerConfigImplMBean;
import org.jboss.system.server.ServerImplMBean;
import org.jboss.system.server.jmx.LazyMBeanServer;
import org.jboss.util.JBossObject;

/**
 * A pojo that creates a legacy jmx kernel ala the jboss-4.x server bootstrap.
 * This is used to support the SARDeployer and mbean integration.
 *
 * @author Scott.Stark@jboss.org
 * @author bill@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class JMXKernel extends JBossObject implements JMXKernelMBean, NotificationEmitter
{
   /**
    * The JMX MBeanServer which will serve as our communication bus.
    */
   private MBeanServer mbeanServer;
   private ServiceController controller;
   private Kernel kernel;
   private ServerConfig serverConfig;
   private NotificationBroadcasterSupport broadcasterSupport = new NotificationBroadcasterSupport();
   private boolean started;

   public ServiceControllerMBean getServiceController()
   {
      return this.controller;
   }

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   public ServerConfig getServerConfig()
   {
      return serverConfig;
   }

   public void setServerConfig(ServerConfig serverConfig)
   {
      this.serverConfig = serverConfig;
   }

   /**
    * We don't want to override platforms default mechanism for creating MBeanServer so lets just do it ourselves
    *
    * @param domain the domain
    * @return the mbeanserver
    * @throws Exception for any error
    */
   private MBeanServer createMBeanServer(String domain) throws Exception
   {
      MBeanServer server;

      String builder = System.getProperty(ServerConstants.MBEAN_SERVER_BUILDER_CLASS_PROPERTY, ServerConstants.DEFAULT_MBEAN_SERVER_BUILDER_CLASS);
      System.setProperty(ServerConstants.MBEAN_SERVER_BUILDER_CLASS_PROPERTY, builder);

      // Check if we'll use the platform MBeanServer or instantiate our own
      if (serverConfig.getPlatformMBeanServer() == true)
      {
         // jdk1.5+
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class clazz = cl.loadClass("java.lang.management.ManagementFactory");
         Class[] sig = null;
         Method method = clazz.getMethod("getPlatformMBeanServer", sig);
         Object[] args = null;
         server = (MBeanServer) method.invoke(null, args);
         // Tell the MBeanServerLocator to point to this mbeanServer
         MBeanServerLocator.setJBoss(server);
         /* If the LazyMBeanServer was used, we need to reset to the jboss
         MBeanServer to use our implementation for the jboss services.
         */
         server = LazyMBeanServer.resetToJBossServer(server);
      }
      else
      {
         // Create our own MBeanServer
         server = MBeanServerFactory.createMBeanServer(domain);
      }
      log.debug("Created MBeanServer: " + server);

      return server;
   }

   public static void setupUrlHandlers()
   {
      String pkgs = System.getProperty("java.protocol.handler.pkgs");
      if (pkgs == null || pkgs.trim().length() == 0)
      {
         pkgs = "org.jboss.net.protocol";
         System.setProperty("java.protocol.handler.pkgs", pkgs);
      }
      else if (!pkgs.contains("org.jboss.net.protocol"))
      {
         pkgs += "|org.jboss.net.protocol";
         System.setProperty("java.protocol.handler.pkgs", pkgs);
      }
      //Field field = URL.class.getDeclaredField("")
      //URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory());
   }

   public void start() throws Exception
   {
      setupUrlHandlers();
      mbeanServer = createMBeanServer("jboss");
      MBeanServerLocator.setJBoss(mbeanServer);

      mbeanServer.registerMBean(new JMXClassLoader(Thread.currentThread().getContextClassLoader()), ServiceDeployer.DEFAULT_CLASSLOADER_OBJECT_NAME);

      controller = new ServiceController();
      controller.setKernel(kernel);
      controller.setMBeanServer(mbeanServer);
      mbeanServer.registerMBean(controller, new ObjectName("jboss.system:service=ServiceController"));
      // Register mbeanServer components
      mbeanServer.registerMBean(this, ServerImplMBean.OBJECT_NAME);
      mbeanServer.registerMBean(new ServerConfigImpl(serverConfig), ServerConfigImplMBean.OBJECT_NAME);
      started = true;
   }

   /**
    * Stop the mbeans
    *
    * @throws IllegalStateException - if not started.
    */
   public void stop() throws IllegalStateException
   {
      if (log.isTraceEnabled())
         log.trace("stop caller:", new Throwable("Here"));

      if (!started)
         throw new IllegalStateException("Server not started");

      log.debug("Shutting down all services");
      shutdownServices();

      // Make sure all mbeans are unregistered
      removeMBeans();
   }

   /**
    * The <code>shutdownServices</code> method calls the one and only
    * ServiceController to shut down all the mbeans registered with it.
    */
   protected void shutdownServices()
   {
      try
      {
         // get the deployed objects from ServiceController
         controller.shutdown();
      }
      catch (Exception e)
      {
         Throwable t = JMXExceptionDecoder.decode(e);
         log.error("Failed to shutdown services", t);
      }
   }

   /**
    * The <code>removeMBeans</code> method uses the mbean mbeanServer to unregister
    * all the mbeans registered here.
    */
   protected void removeMBeans()
   {
      try
      {
         mbeanServer.unregisterMBean(ServiceControllerMBean.OBJECT_NAME);
      }
      catch (Exception e)
      {
         Throwable t = JMXExceptionDecoder.decode(e);
         log.error("Failed to unregister mbeans", t);
      }
   }

   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
   {
      broadcasterSupport.addNotificationListener(listener, filter, handback);
   }

   public void removeNotificationListener(NotificationListener listener)
           throws ListenerNotFoundException
   {
      broadcasterSupport.removeNotificationListener(listener);
   }

   public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
           throws ListenerNotFoundException
   {
      broadcasterSupport.removeNotificationListener(listener, filter, handback);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return broadcasterSupport.getNotificationInfo();
   }

   public void sendNotification(Notification notification)
   {
      broadcasterSupport.sendNotification(notification);
   }

   public void runGarbageCollector()
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void runFinalization()
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void traceMethodCalls(Boolean flag)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void traceInstructions(Boolean flag)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public Date getStartDate()
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public String getVersion()
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public String getVersionName()
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public String getBuildNumber()
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public String getBuildJVM()
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public String getBuildOS()
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public String getBuildID()
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public String getBuildDate()
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public boolean isInShutdown()
   {
      return false;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public void init(Properties props) throws IllegalStateException, Exception
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public ServerConfig getConfig() throws IllegalStateException
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public boolean isStarted()
   {
      return false;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public void shutdown() throws IllegalStateException
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void exit(int exitcode)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void exit()
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void halt(int exitcode)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void halt()
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public String getSpecificationVersion()
   {
      return serverConfig.getSpecificationVersion();
   }

   public String getVersionNumber()
   {
      return "";
   }
}
