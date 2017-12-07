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

import java.util.Timer;
import java.util.TimerTask;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import org.jboss.logging.Logger;
import org.jboss.mx.notification.AsynchNotificationBroadcasterSupport;
import org.jboss.mx.notification.NotificationFilterProxy;
import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.util.threadpool.BlockingMode;
import org.w3c.dom.Element;

/**
 * Used in JMX invoker adaptor test.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 81023 $
 * @jmx:mbean name="jboss.test:service=InvokerTest"
 */
public class InvokerTest
      extends AsynchNotificationBroadcasterSupport
      implements InvokerTestMBean
{
   static Logger log = Logger.getLogger(InvokerTest.class);

   private CustomClass custom = new CustomClass("InitialValue");
   private NonserializableClass custom2 = new NonserializableClass();
   private Element xml;

   public InvokerTest()
   {
      BasicThreadPool pool = new BasicThreadPool();
      pool.setBlockingMode(BlockingMode.RUN);
      pool.setMaximumQueueSize(20);
      pool.setMaximumPoolSize(1);
      super.setThreadPool(pool);
      /* With this set to 0, the testNotificationWithBadListener in
      JMXInvokerUnitTestCase should fail due to the BadListener blocking the
      server notification thread pool. With a value of
      */
      super.setNotificationTimeout(1000);
   }

   /**
    * @jmx:managed-attribute
    */
   public String getSomething()
   {
      return "something";
   }

   public void addNotificationListener(NotificationListener listener,
                                       NotificationFilter filter, Object handback)
   {
      log.info("addNotificationListener, listener: " + listener + ", handback: " + handback);
      super.addNotificationListener(listener, filter, handback);
      if(filter instanceof NotificationFilterProxy)
      {
         NotificationFilter delegateFilter = ((NotificationFilterProxy)filter).getFilter();
         if(delegateFilter instanceof RunTimerFilter)
         {
            Timer t = new Timer();
            Send10Notifies task = new Send10Notifies();
            t.scheduleAtFixedRate(task, 0, 1000);
         }
      }
   }

   public void removeNotificationListener(NotificationListener listener)
         throws ListenerNotFoundException
   {
      log.info("removeNotificationListener, listener: " + listener);
      super.removeNotificationListener(listener);
   }

   /**
    * @jmx:managed-attribute
    */
   public CustomClass getCustom()
   {
      return custom;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setCustom(CustomClass custom)
   {
      this.custom = custom;
   }

   /**
    * @jmx:managed-attribute
    */
   public NonserializableClass getNonserializableClass()
   {
      return custom2;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setNonserializableClass(NonserializableClass custom)
   {
      this.custom2 = custom;
   }

   /**
    * @jmx:managed-attribute
    */
   public Element getXml()
   {
      return xml;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setXml(Element xml)
   {
      this.xml = xml;
   }

   /**
    * @jmx:managed-operation
    */
   public CustomClass doSomething(CustomClass custom)
   {
      return new CustomClass(custom.getValue());
   }

   /**
    * @jmx:managed-operation
    */
   public CustomClass doSomething()
   {
      return new CustomClass(custom.getValue());
   }

   /**
    * @jmx:managed-operation
    */
   public void stop()
   {
      stopThreadPool(true);
   }

   private class Send10Notifies extends TimerTask
   {
      int count;

      /**
       * The action to be performed by this timer task.
       */
      public void run()
      {
         log.info("Sending notification on timer, count=" + count);
         System.out.println("Sending notification on timer, count = " + count);
         Notification notify = new Notification("InvokerTest.timer",
                                                InvokerTest.this, count);
         InvokerTest.super.sendNotification(notify);
         count++;
         if(count == 10)
         {
            super.cancel();
            log.info("Cancelled timer");
         }
      }
   }
}
