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
package org.jboss.test.util.test;

import java.util.Date;

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;
import org.jboss.varia.scheduler.ScheduleManager;
import org.jboss.varia.scheduler.example.SchedulableMBeanExampleMBean;

/**
 * Test case for the Scheduler Utility. The test
 * checks if multiple scheduler can be created,
 * that the notifications goes to the right target
 * and that the reuse of the Scheduler works.
 *
 * @see org.jboss.util.Scheduler
 * @see org.jboss.util.SchedulerMBean
 *
 * @author Andreas Schaefer
 * @author Scott.Stark@jboss.org
 * @version $Revision: 84628 $
 */
public class SchedulerUnitTestCase
   extends JBossTestCase
{

   private String file = "test-default-scheduler-service.xml";

   /**
    * Constructor for the SchedulerUnitTestCase object
    *
    * @param name Test case name
    */
   public SchedulerUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception {
	  super.setUp();
   }

   protected void tearDown() throws Exception {
	  super.tearDown();
   }

   // Public --------------------------------------------------------

   private void registered(ObjectName on) throws Exception
   {
	  assertTrue(on + " isRegistered", getServer().isRegistered(on));
   }

   private SchedulableMBeanExampleMBean get(ObjectName on) throws Exception
   {
	  SchedulableMBeanExampleMBean ex = (SchedulableMBeanExampleMBean)
		 MBeanServerInvocationHandler.newProxyInstance(getServer(), on,
			   SchedulableMBeanExampleMBean.class, false);
	  return ex;
   }

   private void check(ObjectName on, int hits, long remaining) throws Exception
   {
	  SchedulableMBeanExampleMBean ex = get(on);
	  assertNotNull("name " + on, ex);

	  assertEquals("hits", hits, ex.getHitCount());

	  Date hd = ex.getHitDate();
	  if (hits > 0)
	  {
		 Date now = new Date();
		 assertNotNull("hit date", hd);
		 assertTrue("date " + hd + " " + now, !hd.after(now));
		 assertTrue("note", ex.getHitNotification() != null);
		 assertTrue("sched", ex.getSchedulerName() != null);
		 assertEquals("remaining", remaining, ex.getRemainingRepetitions());
	  }
	  assertEquals(null, ex.getTestString());
   }

   /**
    * Tests the default scheduler.
    */
   public void testDefaultScheduler()
      throws Exception
   {
	  ObjectName ex1 = new ObjectName("test:name=SchedulableMBeanExample,instance=1");
	  ObjectName ex2 = new ObjectName("test:name=SchedulableMBeanExample,instance=2");
	  ObjectName ex3 = new ObjectName("test:name=SchedulableMBeanExample,instance=3");
	  ObjectName scheduler0 = new ObjectName("test:service=Scheduler");
	  ObjectName scheduler1 = new ObjectName("test:service=Scheduler,name=SchedulableMBeanExample,instance=1");
	  ObjectName scheduler2 = new ObjectName("test:service=Scheduler,name=SchedulableMBeanExample,instance=2");
	  ObjectName manager1 = new ObjectName("test:service=Scheduler,name=ScheduleManager");

	  deploy(file);
      try
      {
		 registered(new ObjectName(ScheduleManager.DEFAULT_TIMER_NAME));
		 registered(ex1);
		 registered(ex2);
		 registered(ex3);
		 registered(scheduler0);
		 registered(scheduler1);
		 registered(scheduler2);
		 registered(manager1);

		 Thread.sleep(2500); // Half of one period
		 check(ex1, 1, 0); // Only one
		 check(ex2, 0, 0); // StartAtStartup is false
		 check(ex3, 1, 2); // First of three

		 // Sleep for the remainder of the period and start the other schedule
		 Thread.sleep(2500);
		 invoke(scheduler2, "startSchedule", null, null);

		 Thread.sleep(2500); // Half of one period
		 check(ex1, 1, 0); // done
		 check(ex2, 1, -1); // first
		 check(ex3, 2, 1); // one more to go

		 Thread.sleep(5000); // one period
		 check(ex1, 1, 0); // done
		 check(ex2, 2, -1); // second
		 check(ex3, 3, 0); // done

		 Thread.sleep(5000); // one period
         check(ex1, 1, 0); // done
		 check(ex2, 3, -1); // third
         check(ex3, 3, 0); // done

		 // Make sure the schedule stops
         invoke(scheduler2, "stopSchedule", new Object[] { Boolean.TRUE }, new String[] { boolean.class.getName() });
		 Thread.sleep(6000); // a bit more than one period
         check(ex1, 1, 0); // done
		 check(ex2, 3, -1); // no more
         check(ex3, 3, 0); // done

		 // Test it restarts
         invoke(scheduler2, "startSchedule", null, null);
		 Thread.sleep(2500); // half of one period
         check(ex1, 1, 0); // done
		 check(ex2, 4, -1); // restated
         check(ex3, 3, 0); // done
      }
      finally
      {
         undeploy(file);
      }
   }

   /**
	* Test the deployment of a ear containing a sar which creates an
    * instance of the org.jboss.varia.scheduler.Scheduler service with a
    * Schedulable class that exists in an external jar referenced by the
    * sar manifest.
    *
    * @throws Exception
   public void testExternalServiceJar() throws Exception
   {
      // Deploy the external jar containg the Schedulable
      deploy("scheduler.jar");
      // Deploy the ear/sar
      deploy("scheduler.ear");

      try
      {
         ObjectName scheduler = new ObjectName("test:service=TestScheduler");
         assertTrue("test:service=TestScheduler isRegistered",
            getServer().isRegistered(scheduler));
      }
      finally
      {
         undeploy("scheduler.ear");
         undeploy("scheduler.jar");
      }
   }
    */
}
