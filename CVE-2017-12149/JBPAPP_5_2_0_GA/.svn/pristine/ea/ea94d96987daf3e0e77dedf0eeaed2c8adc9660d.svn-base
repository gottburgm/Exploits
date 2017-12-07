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
package org.jboss.test.txtimer.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;

/**
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class TimerSerializationTestCase extends TimerTestBase
{
   public TimerSerializationTestCase(String name)
   {
      super(name);
   }

   public void testTimerSerialization() throws Exception
   {
      TimedMockObject to = new TimedMockObject();
      TimerService service = createTimerService(to);
      Timer timer = service.createTimer(500, null);
      timer.cancel();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      try
      {
         oos.writeObject(timer);
         fail("NotSerializableException expected");
      }
      catch (NotSerializableException expected)
      {
      }
   }

   public void testTimerHandleSerialization() throws Exception
   {
      TimedMockObject to = new TimedMockObject();
      TimerService service = createTimerService(to);
      Timer timer1 = service.createTimer(500, null);
      TimerHandle handle1 = timer1.getHandle();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(handle1);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      TimerHandle handle2 = (TimerHandle)ois.readObject();

      Timer timer2 = handle2.getTimer();
      assertEquals("Timers are not equal", timer1, timer2);

      sleep(1000);
      assertTrue("TimedObject not called", 1 == to.getCallCount());
      assertEquals("Expected no txtimer", 0, service.getTimers().size());
   }
}
