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
package org.jboss.test.jca.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.BaseConnectionManager2;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.ConnectionListener;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.resource.connectionmanager.NoTxConnectionManager;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool.BasePool;
import org.jboss.test.jca.adapter.TestConnectionRequestInfo;
import org.jboss.test.jca.adapter.TestManagedConnectionFactory;

/**
 *  Unit Test for class ManagedConnectionPool
 *
 *
 * Created: Wed Jan  2 00:06:35 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */
public class BaseConnectionManagerUnitTestCase extends TestCase
{

   Logger log = Logger.getLogger(getClass());


   Subject subject = new Subject();
   ConnectionRequestInfo cri = new TestConnectionRequestInfo();
   CachedConnectionManager ccm = new CachedConnectionManager();

   private boolean failed;
   private String failedDescription;
   private Exception failedException;

   /**
    * Creates a new <code>BaseConnectionManagerUnitTestCase</code> instance.
    *
    * @param name test name
    */
   public BaseConnectionManagerUnitTestCase (String name)
   {
      super(name);
   }


   private BaseConnectionManager2 getCM(InternalManagedConnectionPool.PoolParams pp) throws Exception
   {
      return getCM(pp, 0, 5000);
   }

   private BaseConnectionManager2 getCM(InternalManagedConnectionPool.PoolParams pp, int ar, int arwm) throws Exception
   {
      ManagedConnectionFactory mcf = new TestManagedConnectionFactory();
      ManagedConnectionPool poolingStrategy = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);
      BaseConnectionManager2 cm = new NoTxConnectionManager(ccm, poolingStrategy);
      cm.setAllocationRetry(ar);
      cm.setAllocationRetryWaitMillis(arwm);
      poolingStrategy.setConnectionListenerFactory(cm);
      
      if (pp.prefill)
      {
         BasePool bp = (BasePool)poolingStrategy;
         bp.prefill(null, null, false);
      }
      
      return cm;
   }

   private void shutdown(BaseConnectionManager2 cm)
   {
      JBossManagedConnectionPool.OnePool pool = (JBossManagedConnectionPool.OnePool) cm.getPoolingStrategy();
      pool.shutdown();
   }

   public void testGetManagedConnections() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 5;
      pp.blockingTimeout = 100;
      pp.idleTimeout = 500;
      BaseConnectionManager2 cm = getCM(pp);
      try
      {
         ArrayList cs = new ArrayList();
         for (int i = 0; i < pp.maxSize; i++)
         {
            ConnectionListener cl = cm.getManagedConnection(null, null);
            assertTrue("Got a null connection!", cl.getManagedConnection() != null);
            cs.add(cl);
         } // end of for ()
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);
         try
         {
            cm.getManagedConnection(null, null);
            fail("Got a connection more than maxSize!");
         }
         catch (ResourceException re)
         {
            //expected
         } // end of try-catch
         for (Iterator i = cs.iterator(); i.hasNext();)
         {
            cm.returnManagedConnection((ConnectionListener)i.next(), true);
         } // end of for ()
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 0);
      }
      finally
      {
         shutdown(cm);
      }
   }

   public void testIdleTimeout() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 5;
      pp.blockingTimeout = 10;
      pp.idleTimeout = 1000;
      BaseConnectionManager2 cm = getCM(pp);
      try
      {
         Collection mcs = new ArrayList(pp.maxSize);
         for (int i = 0 ; i < pp.maxSize; i++)
            mcs.add(cm.getManagedConnection(subject, cri));
         for (Iterator i =  mcs.iterator(); i.hasNext(); )
            cm.returnManagedConnection((ConnectionListener)i.next(), false);

         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);
         // Let the idle remover kick in
         Thread.sleep(2500);
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 0);
      }
      finally
      {
         shutdown(cm);
      }
   }

   public void testPartialIdleTimeout() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 5;
      pp.blockingTimeout = 10;
      pp.idleTimeout = 2000;
      BaseConnectionManager2 cm = getCM(pp);
      try
      {
         Collection mcs = new ArrayList(pp.maxSize);
         for (int i = 0 ; i < pp.maxSize; i++)
            mcs.add(cm.getManagedConnection(subject, cri));
         for (Iterator i =  mcs.iterator(); i.hasNext(); )
            cm.returnManagedConnection((ConnectionListener)i.next(), false);

         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);
         Thread.sleep(1500);
         ConnectionListener cl = cm.getManagedConnection(subject, cri);
         cm.returnManagedConnection(cl, false);

         // Let the idle remover kick in
         Thread.sleep(1500);
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 1);

         // Let the idle remover kick in
         Thread.sleep(1500);
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 0);
      }
      finally
      {
         shutdown(cm);
      }
   }

   public void testFillToMin() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 3;
      pp.maxSize = 5;
      pp.blockingTimeout = 10;
      pp.idleTimeout = 2000;
      BaseConnectionManager2 cm = getCM(pp);
      try
      {
         ConnectionListener cl = cm.getManagedConnection(subject, cri);
         cm.returnManagedConnection(cl, false);
         // Allow fill to min to work
         Thread.sleep(1000);
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.minSize);
         // Allow the idle remover to work
         Thread.sleep(3000);
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.minSize);
      }
      finally
      {
         shutdown(cm);
      }
   }
   
   public void testPrefillPool() throws Exception{

      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 1;
      pp.maxSize = 5;
      pp.blockingTimeout = 10;
      pp.idleTimeout = 2000;
      pp.prefill = true;
      
      BaseConnectionManager2 cm = getCM(pp);
      //Sleep to let pool filler do it's job
      Thread.sleep(3000);
      assertTrue("Prefilled pool: " + cm.getConnectionCount(), pp.minSize == cm.getConnectionCount());
      
   }
   
   public void testNonStrictMinPool() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 10;
      pp.maxSize = 15;
      pp.blockingTimeout = 10;
      pp.idleTimeout = 1000;
      pp.prefill = true;
      
      BaseConnectionManager2 cm = getCM(pp);
      JBossManagedConnectionPool.OnePool pool = (JBossManagedConnectionPool.OnePool) cm.getPoolingStrategy();
      Thread.sleep(5000);
      assertTrue("Non StrictMin pool should allow destroyed connections below minimum connections", pool.getConnectionDestroyedCount() > 0);

   }
   
   public void testStrictMinPool() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 10;
      pp.maxSize = 15;
      pp.blockingTimeout = 10;
      pp.idleTimeout = 1000;
      pp.prefill = true;
      pp.stictMin = true;
      
      BaseConnectionManager2 cm = getCM(pp);
      JBossManagedConnectionPool.OnePool pool = (JBossManagedConnectionPool.OnePool) cm.getPoolingStrategy();
      //Let Idle remover run
      Thread.sleep(3500);
      assertTrue("StrictMin pool should not destroy below minimum connections", pool.getConnectionDestroyedCount() == 0);
            
   }
   
   public void testMisConfiguredFillToMin() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 6;
      pp.maxSize = 5;
      pp.blockingTimeout = 10;
      pp.idleTimeout = 2000;
      BaseConnectionManager2 cm = getCM(pp);
      try
      {
         ConnectionListener cl = cm.getManagedConnection(subject, cri);
         cm.returnManagedConnection(cl, false);
         // Allow fill to min to work
         Thread.sleep(1000);
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);
         // Allow the idle remover to work
         Thread.sleep(3000);
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);
      }
      finally
      {
         shutdown(cm);
      }
   }

   public void testChangedMaximum() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 5;
      pp.blockingTimeout = 100;
      pp.idleTimeout = 0;
      BaseConnectionManager2 cm = getCM(pp);
      JBossManagedConnectionPool.OnePool pool = (JBossManagedConnectionPool.OnePool) cm.getPoolingStrategy();
      try
      {
         // Checkout all the connections
         ArrayList cs = new ArrayList();
         for (int i = 0; i < pp.maxSize; i++)
         {
            ConnectionListener cl = cm.getManagedConnection(null, null);
            assertTrue("Got a null connection!", cl.getManagedConnection() != null);
            cs.add(cl);
         }
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);

         // Reconfigure
         pp.maxSize = 6;
         pool.flush();

         // Put the connections back (should destroy/close them with no errors)
         for (Iterator i = cs.iterator(); i.hasNext();)
         {
            cm.returnManagedConnection((ConnectionListener)i.next(), true);
         }

         // Checkout all the connections with the new maximum size
         cs = new ArrayList();
         for (int i = 0; i < pp.maxSize; i++)
         {
            ConnectionListener cl = cm.getManagedConnection(null, null);
            assertTrue("Got a null connection!", cl.getManagedConnection() != null);
            cs.add(cl);
         }
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);

         try
         {
            cm.getManagedConnection(null, null);
            fail("Got a connection more than maxSize!");
         }
         catch (ResourceException expected)
         {
         }

         // Put the connections back into the new pool
         for (Iterator i = cs.iterator(); i.hasNext();)
            cm.returnManagedConnection((ConnectionListener)i.next(), true);
         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 0);
      }
      finally
      {
         shutdown(cm);
      }
   }

   public void testAllocationRetry() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 5;
      pp.blockingTimeout = 100;
      pp.idleTimeout = 500;
      BaseConnectionManager2 cm = getCM(pp, 1, 1000);

      assertEquals("Wrong allocation retry value", 1, cm.getAllocationRetry());
      assertEquals("Wrong allocation retry wait millis value", 1000, cm.getAllocationRetryWaitMillis());

      try
      {
         ArrayList cs = new ArrayList();
         for (int i = 0; i < pp.maxSize; i++)
         {
            ConnectionListener cl = cm.getManagedConnection(null, null);
            assertTrue("Got a null connection!", cl.getManagedConnection() != null);
            cs.add(cl);
         }

         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);

         try
         {
            cm.getManagedConnection(null, null);
            fail("Got a connection more than maxSize!");
         }
         catch (ResourceException ignore)
         {
         }

         for (Iterator i = cs.iterator(); i.hasNext();)
         {
            cm.returnManagedConnection((ConnectionListener)i.next(), true);
         }

         assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 0);
      }
      finally
      {
         shutdown(cm);
      }
   }

   public void testAllocationRetryMultiThread() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 6;
      pp.blockingTimeout = 100;
      pp.idleTimeout = 2000;

      final BaseConnectionManager2 cm = getCM(pp, 1, 1000);

      assertEquals("Wrong allocation retry value", 1, cm.getAllocationRetry());
      assertEquals("Wrong allocation retry wait millis value", 1000, cm.getAllocationRetryWaitMillis());

      final int numOfThreads = 2;
      final int iterations = pp.maxSize / numOfThreads;

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch cont = new CountDownLatch(1);
      final CountDownLatch firstPart = new CountDownLatch(numOfThreads);
      final CountDownLatch done = new CountDownLatch(numOfThreads);

      failed = false;
      failedDescription = null;
      failedException = null;

      try
      {
         for (int i = 0; i < numOfThreads; i++)
         {
            Runnable t = new Runnable()
            {
               public void run()
               {
                  List cs = new ArrayList();

                  try
                  {
                     start.await();

                     for (int i = 0; i < iterations; i++)
                     {
                        try
                        {
                           ConnectionListener cl = cm.getManagedConnection(null, null);
                           assertTrue("Got a null connection!", cl.getManagedConnection() != null);
                           cs.add(cl);
                        }
                        catch (ResourceException re)
                        {
                           failed = true;
                           failedDescription = "Failed to get a connection";
                           failedException = re;
                        }
                     }

                     assertEquals("1: Wrong number of connections", iterations, cs.size());

                     firstPart.countDown();

                     cont.await();

                     for (Iterator i = cs.iterator(); i.hasNext();)
                     {
                        cm.returnManagedConnection((ConnectionListener)i.next(), true);
                     }

                     done.countDown();
                  }
                  catch (InterruptedException ie)
                  {
                  }
               }
            };
            new Thread(t).start();
         }
         start.countDown();

         firstPart.await();
         assertTrue("2: Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);

         try
         {
            ConnectionListener cl = cm.getManagedConnection(null, null);
            cm.returnManagedConnection(cl, true);

            failedDescription = "Got a connection";
            failedException = new Exception(cl.toString());
            failed = true;
         }
         catch (ResourceException ignore)
         {
         }

         cont.countDown();

         done.await();

         assertTrue("3: Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 0);

         if (failed)
         {
            if (failedDescription == null)
            {
               fail("Failed criteria: " + failedException);
            }
            else
            {
               fail(failedDescription + ": " + failedException);
            }
         }
      }
      finally
      {
         shutdown(cm);
      }
   }
}
