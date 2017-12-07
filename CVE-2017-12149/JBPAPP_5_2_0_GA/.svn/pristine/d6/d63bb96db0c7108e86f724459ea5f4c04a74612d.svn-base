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
package test.sample.simple;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public class Server
{
   public static void main(String[] args) throws Exception
   {
      MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();

      int registryPort = Registry.REGISTRY_PORT;
      Registry rmiRegistry = LocateRegistry.createRegistry(registryPort);

      String jndiPath = "/jmxconnector";
      JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:" + registryPort + jndiPath);

      // create new connector server and start it
      JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbeanServer);
      connectorServer.start();

      System.out.println("Connector server started.");

      //UnicastRemoteObject.unexportObject(rmiRegistry, true);
   }
}