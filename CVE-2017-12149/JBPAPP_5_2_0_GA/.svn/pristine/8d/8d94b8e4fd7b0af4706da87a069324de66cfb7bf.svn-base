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
package org.jboss.test.util.test.jbas8382;

import java.util.Date;

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;
import org.jboss.varia.scheduler.ScheduleManager;
import org.jboss.varia.scheduler.SingleScheduleProviderMBean;
import org.jboss.varia.scheduler.example.SchedulableMBeanExampleMBean;

/**
 * JBAS-8382 ScheduleManager's skip repeats can be negative
 *
 * @see org.jboss.varia.scheduler.ScheduleManager
 *
 * @author Toshiya Kobayashi
 * @version $Revision: 84000 $
 */
public class JBAS8382UnitTestCase
   extends JBossTestCase
{
   private String file = "test-jbas8382-scheduler-service.xml";

   /**
    * Constructor for the JBAS8382UnitTestCase object
    *
    * @param name Test case name
    */
   public JBAS8382UnitTestCase(String name)
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
    * Tests ScheduleManager when (now - startDate) > Integer.MAX_VALUE and period == 1
    */
   public void testScheduleManagerWithIntegerOverflow()
      throws Exception
   {
	  ObjectName ex = new ObjectName("test:name=SchedulableMBeanExample");
	  ObjectName manager = new ObjectName("test:service=Scheduler,name=ScheduleManager");
          ObjectName provider = new ObjectName("test:service=SingleScheduleProvider");

	  deploy(file);
      try
      {
		 registered(new ObjectName(ScheduleManager.DEFAULT_TIMER_NAME));
		 registered(ex);
		 registered(manager);
		 registered(provider);

                 // JBAS-8382 if (now - startDate) > Integer.MAX_VALUE and period == 1, then skip repeats can be negative and it causes wrong (too much) repetition
                 SingleScheduleProviderMBean pr = (SingleScheduleProviderMBean)
                     MBeanServerInvocationHandler.newProxyInstance(getServer(), provider,
                     SingleScheduleProviderMBean.class, false);
                 pr.setStartDate(String.valueOf(System.currentTimeMillis() - Integer.MAX_VALUE - 1));
                 pr.stop();
                 pr.start();

		 Thread.sleep(1000); // just wait for a while
		 check(ex, 0, 0); // No repetitions left because start date is in the past and could not be reached by Initial Repetitions * Schedule Period 
		 
      }
      finally
      {
         undeploy(file);
      }
   }

}
