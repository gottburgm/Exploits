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
package test.performance.serialize;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.performance.serialize.support.Standard;

/**
 * Tests the size and speed of ObjectName serialization
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class ObjectInstanceTestSuite
   extends TestSuite
{
   // Attributes ----------------------------------------------------------------

   // Constructor ---------------------------------------------------------------

   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(suite());
   }

   /**
    * Construct the tests
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite("All Object Instance tests");

      MBeanServer server = null;

      try
      {
         server = MBeanServerFactory.createMBeanServer();
         ObjectName name = new ObjectName("a:a=a");
         ObjectInstance instance = server.registerMBean(new Standard(), name);
         // Speed Tests
         suite.addTest(new SerializeTEST("testIt", instance, "ObjectInstance"));
      }
      catch (Exception e)
      {
         throw new Error(e.toString());
      }
      finally
      {
         MBeanServerFactory.releaseMBeanServer(server);
      }

      return suite;
   }
}
