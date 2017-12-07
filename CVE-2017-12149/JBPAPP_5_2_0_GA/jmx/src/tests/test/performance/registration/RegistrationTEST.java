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
package test.performance.registration;

import junit.framework.TestCase;

import test.performance.PerformanceSUITE;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * Tests the speed of registrion
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class RegistrationTEST
   extends TestCase
{
   // Attributes ----------------------------------------------------------------

   /**
    * The object to register
    */
   private Object obj;

   /**
    * The object name to register
    */
   private String name;

   /**
    * The description of the test
    */
   private String desc;

   // Constructor ---------------------------------------------------------------

   /**
    * Construct the test
    */
   public RegistrationTEST(String s, Object obj, String name, String desc)
   {
      super(s);
      this.obj = obj;
      this.name = name;
      this.desc = desc;
   }

   /**
    * Test Registration
    */
   public void testIt()
   {
      System.out.println("\n" + desc);
      System.out.println(PerformanceSUITE.REGISTRATION_ITERATION_COUNT + " Registrations/Deregistrations, Repeat: x" + PerformanceSUITE.REPEAT_COUNT);
      System.out.println("(this may take a while...)");

      long start = 0, end = 0;
      float avg = 0l;
      int size = 0;

      MBeanServer server = MBeanServerFactory.createMBeanServer();
      try
      {
         ObjectName on = new ObjectName(name);

         // drop the first batch (+1)
         for (int testIterations = 0; testIterations < PerformanceSUITE.REPEAT_COUNT + 1; ++testIterations)
         {
            start = System.currentTimeMillis();
            for (int invocationIterations = 0; invocationIterations < PerformanceSUITE.REGISTRATION_ITERATION_COUNT; ++invocationIterations)
            {
               server.registerMBean(obj, on);
               server.unregisterMBean(on);
            }
            end = System.currentTimeMillis();

            if (testIterations != 0)
            {
               long time = end - start;
               System.out.print( time + " ");
               avg += time;
            }
         }

         System.out.println("\nAverage: " + (avg/PerformanceSUITE.REPEAT_COUNT));
      }
      catch (Exception e)
      {
         fail(e.toString());
      }
      finally
      {
         MBeanServerFactory.releaseMBeanServer(server);
      }
   }
}
