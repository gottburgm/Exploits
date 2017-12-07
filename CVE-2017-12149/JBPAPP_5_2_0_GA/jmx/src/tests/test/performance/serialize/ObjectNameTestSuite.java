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

import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the size and speed of ObjectName serialization
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class ObjectNameTestSuite
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
      TestSuite suite = new TestSuite("All Object Name tests");

      try
      {
         ObjectName name1 = new ObjectName("a:a=a");
         ObjectName name10 = new ObjectName("a:a=a,b=b,c=c,d=d,e=e,f=f,g=g,h=h,i=i,j=j");
         StringBuffer buffer = new StringBuffer("a:0=0");
         for (int i=1; i < 100; i++)
            buffer.append("," + i + "=" + i);
         ObjectName name100 = new ObjectName(buffer.toString());
         // Speed Tests
         suite.addTest(new SerializeTEST("testIt", name1, "ObjectName 1 property"));
         suite.addTest(new SerializeTEST("testIt", name10, "ObjectName 10 properties"));
         suite.addTest(new SerializeTEST("testIt", name100, "ObjectName 100 properties"));
      }
      catch (Exception e)
      {
         throw new Error(e.toString());
      }

      return suite;
   }
}
