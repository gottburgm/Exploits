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
package org.jboss.test.txtimer.test;

import java.util.Date;

import javax.ejb.Timer;
import javax.ejb.TimerService;

/**
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 60899 $
 * @since 07-Apr-2004
 */
public class SimpleTimerTestCase extends TimerTestBase
{
   public SimpleTimerTestCase(String name)
   {
      super(name);
   }

   public void testSingleEventDuration() throws Exception
   {
      TimedMockObject to = new TimedMockObject();
      TimerService service = createTimerService(to);

      service.createTimer(500, null);
      assertEquals("Expected one txtimer", 1, service.getTimers().size());
      sleep(1000);
      assertTrue("TimedObject not called", 1 == to.getCallCount());
      assertEquals("Expected no txtimer", 0, service.getTimers().size());
   }

   public void testSingleEventExpire() throws Exception
   {
      TimedMockObject to = new TimedMockObject();
      TimerService service = createTimerService(to);

      service.createTimer(new Date(System.currentTimeMillis() + 500), null);
      assertEquals("Expected one txtimer", 1, service.getTimers().size());
      sleep(1000);
      assertTrue("TimedObject not called", 1 == to.getCallCount());
      assertEquals("Expected no txtimer", 0, service.getTimers().size());
   }

   public void testMultipleEventDuration() throws Exception
   {
      TimedMockObject to = new TimedMockObject();
      TimerService service = createTimerService(to);

      Timer timer = service.createTimer(500, 500, null);
      assertEquals("Expected one txtimer", 1, service.getTimers().size());
      sleep(2000);
      assertTrue("TimedObject not called enough", 1 < to.getCallCount());

      timer.cancel();
      assertEquals("Expected no txtimer", 0, service.getTimers().size());
   }

   public void testMultipleEventExpire() throws Exception
   {
      TimedMockObject to = new TimedMockObject();
      TimerService service = createTimerService(to);

      Timer timer = service.createTimer(new Date(System.currentTimeMillis() + 500), 500, null);
      assertEquals("Expected one txtimer", 1, service.getTimers().size());
      sleep(2000);
      assertTrue("TimedObject not called enough", 1 < to.getCallCount());

      timer.cancel();
      assertEquals("Expected no txtimer", 0, service.getTimers().size());
   }

}
