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
package org.jboss.test.jmx.serialization;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Serialization tests
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */

public class SerializationSUITE extends TestSuite
{

   public static ClassLoader jmxri;
   public static ClassLoader jbossmx;
   public static int form = 11; // 1.1

   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("All Serialization Tests");

      try
      {
         File riLocation = new File(System.getProperty("jboss.test.location.jmxri"));
         jmxri = new URLClassLoader(new URL[] {riLocation.toURL()},
                                    SerializationSUITE.class.getClassLoader());
         File jbossmxLocation = new File(System.getProperty("jboss.test.location.jbossmx"));
         jbossmx = new URLClassLoader(new URL[] {jbossmxLocation.toURL()},
                                    SerializationSUITE.class.getClassLoader());

         String prop = System.getProperty("jmx.serial.form");
         if (prop != null && prop.equals("1.0"))
            form = 10; // 1.0
         System.err.println("Serialization Tests: jmx.serial.form=" + prop);
      
         suite.addTest(new TestSuite(SerializeTestCase.class));
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e.toString());
      }
      
      return suite;
   }
}
