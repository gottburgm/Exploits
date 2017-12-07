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
package org.jboss.test.session.test;

import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.session.interfaces.CountedSession;
import org.jboss.test.session.interfaces.CountedSessionHome;
import org.jboss.test.session.interfaces.CounterSession;
import org.jboss.test.session.interfaces.CounterSessionHome;

/**
 * Test that ejbRemove is called.
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class EjbRemoveUnitTestCase extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      // JBAS-3607, the execution order of tests in this test case is important
      // so it must be defined explicitly when running under some JVMs
      TestSuite suite = new TestSuite();
      suite.addTest(new EjbRemoveUnitTestCase("testEjbRemoveCalledForEveryCall"));
      suite.addTest(new EjbRemoveUnitTestCase("testEjbRemoveNotCalledForEveryCall"));
      suite.addTest(new EjbRemoveUnitTestCase("testEjbRemoveMultiThread"));      
      
      return JBossTestCase.getDeploySetup(suite, "test-session-remove.jar");      
   }
   
   public EjbRemoveUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * In this test, pooling is disabled (MaximumSize==0) so call
    * to the CountedSession bean should create a new instance,
    * (ejbCreate()) use it but then throw it away (ejbRemove())
    * ather than putting it back to the pool.
    * @throws Exception
    */
   public void testEjbRemoveCalledForEveryCall() throws Exception
   {
      CounterSessionHome counterHome = (CounterSessionHome)getInitialContext().lookup("CounterSession");
      CountedSessionHome countedHome = (CountedSessionHome)getInitialContext().lookup("CountedSession");
      
      CounterSession counter = counterHome.create();
      counter.clearCounters();
      
      CountedSession counted = countedHome.create();
      assertEquals("createCounter", 1, counter.getCreateCounter());
      assertEquals("removeCounter", 1, counter.getRemoveCounter());
      
      counted.doSomething(0);
      assertEquals("createCounter", 2, counter.getCreateCounter());
      assertEquals("removeCounter", 2, counter.getRemoveCounter());      
      
      counted.remove();
      assertEquals("createCounter", 3, counter.getCreateCounter());
      assertEquals("removeCounter", 3, counter.getRemoveCounter());
   }
   
   /**
    * In this test, pooling is enabled (Maximum==5) so after the
    * initial create() call, the same instance should be used
    * from the pool, and only removed when the app gets undeployed
    * @throws Exception
    */
   public void testEjbRemoveNotCalledForEveryCall() throws Exception
   {
      CounterSessionHome counterHome = (CounterSessionHome)getInitialContext().lookup("CounterSession");
      CountedSessionHome countedHome = (CountedSessionHome)getInitialContext().lookup("CountedSession2");
      
      CounterSession counter = counterHome.create();
      counter.clearCounters();
      
      CountedSession counted = countedHome.create();
      assertEquals("createCounter", 1, counter.getCreateCounter());
      assertEquals("removeCounter", 0, counter.getRemoveCounter());
      
      counted.doSomething(0);
      assertEquals("createCounter", 1, counter.getCreateCounter());
      assertEquals("removeCounter", 0, counter.getRemoveCounter());      
      
      counted.remove();
      assertEquals("createCounter", 1, counter.getCreateCounter());
      assertEquals("removeCounter", 0, counter.getRemoveCounter());
   }

   public void testEjbRemoveMultiThread() throws Exception
   {
      CounterSessionHome counterHome = (CounterSessionHome)getInitialContext().lookup("CounterSession");
      CountedSessionHome countedHome = (CountedSessionHome)getInitialContext().lookup("CountedSession2");
      
      CounterSession counter = counterHome.create();
      counter.clearCounters();
      
      final CountedSession counted = countedHome.create();
      
      Runnable runnable = new Runnable() {
         public void run()
         {
            try
            {
               // introduce 1sec delay
               counted.doSomething(1000);
            }
            catch (RemoteException e)
            {
               // ignore
            }
         }
      };

      for (int i = 0; i < 10; i++)
      {
         new Thread(runnable).start();
      }
      
      // since the session pool is Maximum==5, using 10 concurrent
      // requests ensures at least 5 instances will have to be created
      // (ejbCreate() to handle the load. Those 5 extra instances, will also have
      // to be destroyed (ejbRemove()) upon return, because the pool will
      // only store the first 5

      // wait for all 10 threads to finish
      Thread.sleep(2000);
      
      assertTrue("createCounter >= 5", counter.getCreateCounter() >= 5);
      assertTrue("removeCounter == 5", counter.getRemoveCounter() == 5);
   }
}
