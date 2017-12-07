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
package test.performance;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 */

public class PerformanceSUITE extends TestSuite
{
   
   public final static int SECOND          = 1000;
   public final static int THROUGHPUT_TIME = 3 * SECOND;
   
   public final static int ITERATION_COUNT = 100000;
   public final static int REGISTRATION_ITERATION_COUNT = 1000;
   public final static int REPEAT_COUNT = 10;
   public final static int SERIALIZE_ITERATION_COUNT = 1000;
   public final static int TIMER_ITERATION_COUNT = 2000;

   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("All Performance Tests");

      suite.addTest(test.performance.dynamic.DynamicSUITE.suite());
      suite.addTest(test.performance.standard.StandardSUITE.suite());
      suite.addTest(test.performance.modelmbean.ModelMBeanSUITE.suite());
      suite.addTest(test.performance.invocationhandler.InvocationHandlerSUITE.suite());
      suite.addTest(test.performance.serialize.SerializeSUITE.suite());
      suite.addTest(test.performance.registration.RegistrationSUITE.suite());
      suite.addTest(test.performance.timer.TimerSUITE.suite());
      
      return suite;
   }
}
