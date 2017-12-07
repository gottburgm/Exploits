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
package org.jboss.test.cluster.defaultcfg.test;

import java.util.EmptyStackException;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import junit.framework.TestCase;

import org.jboss.test.cluster.haservice.HAServiceMBeanSupportTester;
import org.jboss.test.cluster.testutil.MockHAPartition;

/**
 * 
 * @author   Ivelin Ivanov <ivelin@apache.org>
 *
 */
public class HAServiceMBeanSupportUnitTestCase extends TestCase
{
   private HAServiceMBeanSupportTester haServiceMBeanSupportTester = null;

   public HAServiceMBeanSupportUnitTestCase(String name)
   {
      super(name);
   }
    
   @Override
   public void setUp()
   {
      this.haServiceMBeanSupportTester = new HAServiceMBeanSupportTester();
   }

   
   @Override
   public void tearDown()
   {
      this.haServiceMBeanSupportTester = null;
   }


   /**
    * 
    * messages should be sent out to both remote and local listeners.
    *
    */
   public void testSendNotificationBroadcastsToClusterAndLocally()
   {
      Notification notification = new Notification("test.notification", "some:name=tester", 1);
      this.haServiceMBeanSupportTester.sendNotification(notification);

      assertSame("sendNotificationToLocalListeners() was not handed the original notification", this.haServiceMBeanSupportTester.invocationStack.pop(), notification);

      assertEquals("method not invoked as expected", this.haServiceMBeanSupportTester.invocationStack.pop(), "sendNotificationToLocalListeners");

      assertSame("sendNotificationRemote() was not handed the original notification", this.haServiceMBeanSupportTester.invocationStack.pop(), notification);
      
      assertEquals("method not invoked as expected", this.haServiceMBeanSupportTester.invocationStack.pop(), "sendNotificationRemote");
   }

   /**
    * 
    * Even if the message cannot be sent out to the cluster,
    * it should still be delivered to local listeners.
    *
    */
   public void testSendNotificationAfterClusterFailureContinueWithLocal()
   {
      this.haServiceMBeanSupportTester.shouldSendNotificationRemoteFail = true;

      Notification notification = new Notification("test.notification", "some:name=tester", 1);
      this.haServiceMBeanSupportTester.sendNotification( notification );
      
      assertEquals("sendNotificationToLocalListeners() was not handed the original notification", this.haServiceMBeanSupportTester.invocationStack.pop(), notification );

      assertEquals("method not invoked as expected", this.haServiceMBeanSupportTester.invocationStack.pop(), "sendNotificationToLocalListeners");
   }
   
   public void testSendLifecycleNotifications()
   {
      Notification notification = new AttributeChangeNotification(this.haServiceMBeanSupportTester, 1, System.currentTimeMillis(), "test", "State", "java.lang.Integer", new Integer(0), new Integer(1));
       
      this.haServiceMBeanSupportTester.setSendRemoteLifecycleNotifications(false);
       
      this.haServiceMBeanSupportTester.sendNotification( notification );
       
      assertEquals("sendNotificationToLocalListeners() was handed the original notification", this.haServiceMBeanSupportTester.invocationStack.pop(), notification );

      assertEquals("method invoked as expected", this.haServiceMBeanSupportTester.invocationStack.pop(), "sendNotificationToLocalListeners");

      try
      {
         this.haServiceMBeanSupportTester.invocationStack.pop();
         fail("sendNotificationRemote() was not handed the original notification");
      }
      catch (EmptyStackException good) {}
       
      this.haServiceMBeanSupportTester.setSendRemoteLifecycleNotifications(true);
      this.haServiceMBeanSupportTester.setSendLocalLifecycleNotifications(false);
       
      this.haServiceMBeanSupportTester.sendNotification( notification );

      assertEquals("sendNotificationRemote() was handed the original notification", this.haServiceMBeanSupportTester.invocationStack.pop(), notification );
       
      assertEquals("method invoked as expected", this.haServiceMBeanSupportTester.invocationStack.pop(), "sendNotificationRemote");
       
      try
      {
         this.haServiceMBeanSupportTester.invocationStack.pop();
         fail("sendNotificationToLocalListeners() was not handed the original notification");
      }
      catch (EmptyStackException good) {}
   }
   
   public void testServiceHAName() throws Exception
   {
      assertNull("Initial name correct", this.haServiceMBeanSupportTester.getServiceHAName());

      this.haServiceMBeanSupportTester.setHAPartition(new MockHAPartition());
      this.haServiceMBeanSupportTester.start();
      
      assertEquals("Default name correct", HAServiceMBeanSupportTester.SERVICE_NAME, this.haServiceMBeanSupportTester.getServiceHAName());
       
      this.haServiceMBeanSupportTester.setServiceHAName("Test");
       
      assertEquals("Specified name correct", "Test", this.haServiceMBeanSupportTester.getServiceHAName());
   }
}
