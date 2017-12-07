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
package test.compliance.core.notification.connection;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public class ConnectionNotificationTest extends TestCase implements NotificationListener
{
   private JMXConnectorServer connectorServer;
   private List notificationList = new ArrayList();

   public void setUp() throws Exception
   {
      MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();

      int registryPort = Registry.REGISTRY_PORT;
      Registry rmiRegistry = LocateRegistry.createRegistry(registryPort);

      String jndiPath = "/jmxconnector";
      JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:" + registryPort + jndiPath);

      // create new connector server and start it
      connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbeanServer);
      connectorServer.start();

      System.out.println("Connector server started.");
   }

   public void tearDown() throws Exception
   {
      connectorServer.stop();
   }

   public void testConnectionNotifications() throws Exception
   {
      JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxconnector");

      JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, null);
      connector.addConnectionNotificationListener(this, null, null);
      connector.connect();
      connector.close();

      assertEquals("Should have received two notifications (one for connect and one for close).", 2, notificationList.size());
   }

   public void handleNotification(Notification notification, Object o)
   {
      notificationList.add(notification);
   }
}