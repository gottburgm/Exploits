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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;
import test.performance.PerformanceSUITE;

/**
 * Tests the speed of serialization
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class SerializeTEST
   extends TestCase
{
   // Attributes ----------------------------------------------------------------

   /**
    * The object to test
    */
   private Object obj;

   /**
    * The description of the test
    */
   private String desc;

   // Constructor ---------------------------------------------------------------

   /**
    * Construct the test
    */
   public SerializeTEST(String s, Object obj, String desc)
   {
      super(s);
      this.obj = obj;
      this.desc = desc;
   }

   /**
    * Test Serialization
    */
   public void testIt()
   {
      System.out.println("\n" + desc);
      System.out.println(PerformanceSUITE.SERIALIZE_ITERATION_COUNT + " Serializations, Repeat: x" + PerformanceSUITE.REPEAT_COUNT);
      System.out.println("(this may take a while...)");

      long start = 0, end = 0;
      float avg = 0l;
      int size = 0;

      try
      {

         Object result = null;
         ByteArrayOutputStream baos = new ByteArrayOutputStream();

         // drop the first batch (+1)
         for (int testIterations = 0; testIterations < PerformanceSUITE.REPEAT_COUNT + 1; ++testIterations)
         {
            start = System.currentTimeMillis();
            for (int invocationIterations = 0; invocationIterations < PerformanceSUITE.SERIALIZE_ITERATION_COUNT; ++invocationIterations)
            {
               // Serialize it
               baos.reset();
               ObjectOutputStream oos = new ObjectOutputStream(baos);
               oos.writeObject(obj);
    
               // Deserialize it
               ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
               ObjectInputStream ois = new ObjectInputStream(bais);
               result = ois.readObject();
            }
            end = System.currentTimeMillis();

            if (testIterations != 0)
            {
               long time = end - start;
               System.out.print( time + " ");
               avg += time;
            }
         }

         System.out.print("\nAverage: " + (avg/PerformanceSUITE.REPEAT_COUNT));
         System.out.println("   Size: " + baos.toByteArray().length);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }
   }
}
