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
package test.compliance.core.notification;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ListenerNotFoundException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public class NotificationTest extends TestCase
{
   private JMXConnector connector;
   private JMXConnectorServer connectorServer;
   private MBeanServerConnection server;
   private int registryPort = 1090;
   private String jndiPath = "/jmxconnector";

   public void setUp() throws Exception
   {
      MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();
      InvokerTest test = new InvokerTest();
      mbeanServer.registerMBean(test, getObjectName());

      // check to see if registry already created
      Registry rmiRegistry = LocateRegistry.getRegistry(registryPort);
      if(rmiRegistry != null)
      {
         try
         {
            rmiRegistry.list();
         }
         catch(RemoteException e)
         {
            rmiRegistry = LocateRegistry.createRegistry(registryPort);
         }
      }
      else
      {
         rmiRegistry = LocateRegistry.createRegistry(registryPort);
      }
      JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:" + registryPort + jndiPath);

      // create new connector server and start it
      connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbeanServer);
      connectorServer.start();

      connector = JMXConnectorFactory.connect(url);
      server = connector.getMBeanServerConnection();

   }

   public void tearDown() throws Exception
   {
      if(connector != null)
      {
         connector.close();
      }
      if(connectorServer != null)
      {
         connectorServer.stop();
      }
   }

   private ObjectName getObjectName() throws MalformedObjectNameException
   {
      return new ObjectName("test:type=InvokerTest");
   }

   public void testNotification() throws Exception
   {
      Listener listener = new Listener(10);
      server.addNotificationListener(getObjectName(), listener, new RunTimerFilter(), "runTimer");
      synchronized(listener)
      {
         listener.wait(15000);
      }
      server.removeNotificationListener(getObjectName(), listener);
      int count = listener.getCount();
      assertTrue("Received 10 notifications, count=" + count, count == 10);
   }

   /**
    * Test the remoting of JMX Notifications with a valid listener
    * and a bad listener that attempts to hang the service by sleeping
    * in the notification callback.
    *
    * @throws Exception
    */
   public void testNotificationWithBadListener() throws Exception
   {
      // Add a bad listener
      BadListener badListener = new BadListener();
      server.addNotificationListener(getObjectName(), badListener, null, "BadListener");
      Listener listener = new Listener(10);
      // Add a good listener
      server.addNotificationListener(getObjectName(), listener, new RunTimerFilter(), "runTimer");
      // Wait 25 seconds for the good listener events to complete
      synchronized(listener)
      {
         listener.wait(25000);
      }
      server.removeNotificationListener(getObjectName(), listener);
      int count = listener.getCount();
      assertTrue("Received 10 notifications from Listener, count=" + count,
                 count == 10);
      count = badListener.getCount();
      assertTrue("Received >= 1 notifications from BadListener, count=" + count,
                 count >= 1);
      try
      {
         server.removeNotificationListener(getObjectName(), badListener);
      }
      catch(ListenerNotFoundException e)
      {
         System.out.println("The BadListener was not found");
      }
   }
}