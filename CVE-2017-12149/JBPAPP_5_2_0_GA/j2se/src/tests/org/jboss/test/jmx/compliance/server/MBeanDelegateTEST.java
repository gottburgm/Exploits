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
package org.jboss.test.jmx.compliance.server;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.jboss.test.jmx.compliance.server.support.Test;

/**
 * Tests for the MBean server delegate.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 *
 * @version $Revision: 81019 $
 */
public class MBeanDelegateTEST extends TestCase
{

   public MBeanDelegateTEST(String s)
   {
      super(s);
   }
   
   class MyNotificationListener implements NotificationListener {

      int notificationCount = 0;
      
      public void handleNotification(Notification notification, Object handback)
      {
         try
         {
            notificationCount++;
         }
         catch (Exception e)
         {
            fail("Unexpected error: " + e.toString());
         }
      }
   }

   public synchronized void testRegistrationAndUnregistrationNotification() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MyNotificationListener listener = new MyNotificationListener();
      
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener, null, null
      );       
    
      // force registration notification
      server.registerMBean(new Test(), new ObjectName("test:foo=bar"));
    
      // force unregistration notification
      server.unregisterMBean(new ObjectName("test:foo=bar"));
      
      // wait for notif to arrive max 5 secs
      for (int i = 0; i < 10; ++i)
      {
         wait(500);
         
         if (listener.notificationCount > 1)
            break;
      }
      
      assertTrue(listener.notificationCount == 2);
   }

}
