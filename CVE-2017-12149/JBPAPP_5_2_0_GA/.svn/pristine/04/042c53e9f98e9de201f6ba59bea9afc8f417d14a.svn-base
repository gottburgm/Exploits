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
package org.jboss.mx.remoting.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import org.jboss.logging.Logger;
import org.jboss.net.sockets.DefaultSocketFactory;
/**
 * Service mbean for starting the JMX Remoting (JSR-160) connector server.
 * 
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 */
public class JMXConnectorServerService implements JMXConnectorServerServiceMBean
{
   /**
    * Default jndi path used to specify location of connector server.
    * Value is '/jmxconnector'.
    */
   public static final String JNDI_PATH_DEFAULT = "/jmxconnector";

   private MBeanServer mbeanServer;

   /** The interface to bind, useful for multi-homed hosts */
   private InetAddress bindAddress;   
   private int registryPort = Registry.REGISTRY_PORT;
   private String jndiPath = JNDI_PATH_DEFAULT;
   private JMXConnectorServer connectorServer = null;
   private Registry rmiRegistry = null;

   private static final Logger log = Logger.getLogger(JMXConnectorServerService.class);

   public void setRegistryPort(int registryPort)
   {
      this.registryPort = registryPort;
   }

   public int getRegistryPort()
   {
      return registryPort;
   }

   public void setBindAddress(String bindAddress) throws UnknownHostException
   {
      this.bindAddress = toInetAddress(bindAddress);
   }

   public String getBindAddress()
   {
      if (bindAddress != null)
         return bindAddress.getHostAddress();
      else
         return null;
   }

   public String getJndiPath()
   {
      return jndiPath;
   }

   public void setJndiPath(String jndiPath)
   {
      this.jndiPath = jndiPath;
   }

   public void create() throws Exception
   {
      // do nothing, putting code in start
   }

   public void start() throws Exception
   {
      // the address to expose in the urls
      String host = System.getProperty("java.rmi.server.hostname");
      
      // check to see if registry already created
      rmiRegistry = LocateRegistry.getRegistry(host, registryPort);
      if (rmiRegistry != null)
      {
         try
         {
            rmiRegistry.list();
         }
         catch(RemoteException e)
         {
            log.debug("No registry running at host '" + host +
                  "', port '" + registryPort + "'.  Will create one.");
            rmiRegistry = LocateRegistry.createRegistry(registryPort, null, new DefaultSocketFactory(bindAddress));
         }
      }
      else
      {
         rmiRegistry = LocateRegistry.createRegistry(registryPort, null, new DefaultSocketFactory(bindAddress));
      }

      String serviceURL = "service:jmx:rmi://" + host + "/jndi/rmi://" + host + ":" + registryPort + jndiPath;

      JMXServiceURL url = new JMXServiceURL(serviceURL);

      // create new connector server and start it
      final Map<String, Object> environment = new HashMap<String, Object>();
      final DefaultSocketFactory clientSocketFactory = new DefaultSocketFactory();
      clientSocketFactory.setBindAddress(bindAddress.getHostAddress());
      environment.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, clientSocketFactory);
      connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, environment, mbeanServer);
      connectorServer.start();

      log.info("JMX Connector server: " + serviceURL);
   }

   public void stop() throws IOException
   {
      if(connectorServer != null)
      {
         connectorServer.stop();
      }
   }

   public void destroy()
   {
      // do nothing, putting code in stop
   }

   public ObjectName preRegister(MBeanServer mbeanServer, ObjectName objectName) throws Exception
   {
      this.mbeanServer = mbeanServer;
      return objectName;
   }

   public void postRegister(Boolean aBoolean)
   {
      // no op, needed for MBeanRegistration interface
   }

   public void preDeregister() throws Exception
   {
      // no op, needed for MBeanRegistration interface
   }

   public void postDeregister()
   {
      // no op, needed for MBeanRegistration interface
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