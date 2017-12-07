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

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerDelegateMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      // no host or port, since will be getting rmi stub
      JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxconnector");

      JMXConnector connector = JMXConnectorFactory.connect(url);

      MBeanServerConnection connection = connector.getMBeanServerConnection();

      ObjectName delegateName = ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate");
      Object proxy = MBeanServerInvocationHandler.newProxyInstance(connection, delegateName, MBeanServerDelegateMBean.class, true);
      MBeanServerDelegateMBean delegate = (MBeanServerDelegateMBean) proxy;

      System.out.println("MBeanServer vendor is " + delegate.getImplementationVendor());
      System.out.println("MBeanServer version is " + delegate.getImplementationVersion());
      System.out.println("MBeanServer specification name is " + delegate.getSpecificationName());
      System.out.println("MBeanServer specification vendor is " + delegate.getSpecificationVendor());
      System.out.println("MBeanServer specification version is " + delegate.getSpecificationVersion());
      System.out.println("MBeanServer MBeanCount is " + connection.getMBeanCount());

      ObjectName objName = new ObjectName("test:name=sample");
      connection.createMBean(Sample.class.getName(), objName);
      Object ret = connection.invoke(objName, "doSomething", new Object[] {"foo"}, new String[] {String.class.getName()});
      System.out.println("Return to doSomething() is " + ret);

      NotificationListener listener = new Listener();
      connection.addNotificationListener(objName,listener, null, null);

      ret = connection.invoke(objName, "doSomething", new Object[] {"bar"}, new String[] {String.class.getName()});
      System.out.println("Return to doSomething() is " + ret);

      // give time for notification to come in
      Thread.currentThread().sleep(5000);

      connection.removeNotificationListener(objName, listener);

      System.out.println("Removed notification listener.");

   }


   public static class Listener implements NotificationListener
   {

      /**
       * Callback method from the broadcaster MBean this listener implementation
       * is registered to.
       *
       * @param notification the notification object
       * @param handback     the handback object given to the broadcaster
       *                     upon listener registration
       */
      public void handleNotification(Notification notification, Object handback)
      {
         System.out.println("Got notification: " + notification);
      }
   }
}