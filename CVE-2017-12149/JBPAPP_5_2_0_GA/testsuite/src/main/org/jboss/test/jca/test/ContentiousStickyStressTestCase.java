/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

import javax.resource.cci.Connection;
import javax.transaction.Transaction;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * Contentious pooling stress test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 65496 $
 */
public class ContentiousStickyStressTestCase extends AbstractContentiousPoolingStressTest
{
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(ContentiousStickyStressTestCase.class, "jca-tests.jar");
   }

   public boolean isSticky()
   {
      return true;
   }
   
   public void testContentiousInterleavingStressTestcase() throws Throwable
   {
      tm.setTransactionTimeout(0);
      tm.begin();
      final Transaction tx = tm.suspend();
      ContentiousPoolingRunnable[] runnables = new ContentiousPoolingRunnable[getThreadCount()];
      for (int i = 0; i < runnables.length; ++i)
      {
         runnables[i] = new ContentiousPoolingRunnable(tx)
         {
            public void doRun() throws Throwable
            {
               Connection c = cf.getConnection();
               c.close();
            }
         };
      }

      runConcurrentTest(runnables, new ConcurrentTestCallback()
            {
               public void finished() throws Throwable
               {
                  tm.resume(tx);
                  tm.commit();
               }
            });
   }
   
   public ContentiousStickyStressTestCase(String name)
   {
      super(name);
   }
}
