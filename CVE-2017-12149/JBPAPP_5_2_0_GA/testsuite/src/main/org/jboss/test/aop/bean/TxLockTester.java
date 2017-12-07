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
package org.jboss.test.aop.bean;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 * @see Monitorable
 */
public class TxLockTester
extends ServiceMBeanSupport
implements TxLockTesterMBean, MBeanRegistration
{
   // Constants ----------------------------------------------------
   // Attributes ---------------------------------------------------
   static Logger log = Logger.getLogger(TxLockTester.class);
   MBeanServer m_mbeanServer;

   // Static -------------------------------------------------------
   
   // Constructors -------------------------------------------------
   public TxLockTester()
   {}
   
   // Public -------------------------------------------------------
   
   // MBeanRegistration implementation -----------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   throws Exception
   {
      m_mbeanServer = server;
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {}

   public void preDeregister() throws Exception
   {}

   public void postDeregister()
   {}

   protected void startService()
   throws Exception
   {
   }

   protected void stopService()
   {
   }

   boolean failed = false;

   public class LockThread implements Runnable
   {
      TxLockedPOJO pojo;

      public LockThread(TxLockedPOJO pojo)
      {
         this.pojo = pojo;
      }

      public void run()
      {
         try
         {
            // A lock should be held
            pojo.setField(5);
         }
         catch (Exception ex)
         {
            failed = true;
            log.error("thread failed", ex);
         }
      }
   }

   public class AnnotatedLockThread implements Runnable
   {
      AnnotatedTxLockedPOJO pojo;

      public AnnotatedLockThread(AnnotatedTxLockedPOJO pojo)
      {
         this.pojo = pojo;
      }

      public void run()
      {
         try
         {
            // A lock should be held
            pojo.setField(5);
         }
         catch (Exception ex)
         {
            failed = true;
            log.error("thread failed", ex);
         }
      }
   }

   public void testXml()
   {
      failed = false;
      try
      {
         log.info("TESTING TX LOCK");
         TxLockedPOJO pojo = new TxLockedPOJO();
         Thread t = new Thread(new LockThread(pojo));
         t.start();
         Thread.sleep(1000);
         pojo.setField(6);
         if (failed) throw new RuntimeException("test failed");
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex.getMessage());
      }
   }

   public void testAnnotated()
   {
      failed = false;
      try
      {
         log.info("TESTING TX LOCK");
         AnnotatedTxLockedPOJO pojo = new AnnotatedTxLockedPOJO();
         Thread t = new Thread(new AnnotatedLockThread(pojo));
         t.start();
         Thread.sleep(1000);
         pojo.setField(6);
         if (failed) throw new RuntimeException("test failed");
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex.getMessage());
      }
   }

}

