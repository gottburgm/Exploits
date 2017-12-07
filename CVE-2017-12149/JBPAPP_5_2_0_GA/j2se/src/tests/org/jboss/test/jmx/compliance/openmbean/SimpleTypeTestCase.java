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
package org.jboss.test.jmx.compliance.openmbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.management.ObjectName;
import javax.management.openmbean.SimpleType;

import junit.framework.TestCase;

/**
 * Simple type tests.<p>
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class SimpleTypeTestCase
  extends TestCase
{
   // Static --------------------------------------------------------------------

   static ObjectName objectName;

   static
   {
      try
      {
         objectName = new ObjectName("test:test=test");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.toString());
      }
   }

   // Attributes ----------------------------------------------------------------

   SimpleType[] types = new SimpleType[]
   {
      SimpleType.BIGDECIMAL,
      SimpleType.BIGINTEGER,
      SimpleType.BOOLEAN,
      SimpleType.BYTE,
      SimpleType.CHARACTER,
      SimpleType.DATE,
      SimpleType.DOUBLE,
      SimpleType.FLOAT,
      SimpleType.INTEGER,
      SimpleType.LONG,
      SimpleType.OBJECTNAME,
      SimpleType.SHORT,
      SimpleType.STRING,
      SimpleType.VOID
   };

   Class[] classes = new Class[]
   {
      BigDecimal.class,
      BigInteger.class,
      Boolean.class,
      Byte.class,
      Character.class,
      Date.class,
      Double.class,
      Float.class,
      Integer.class,
      Long.class,
      ObjectName.class,
      Short.class,
      String.class,
      Void.class
   };

   Object[] objects = new Object[]
   {
      new BigDecimal(1),
      BigInteger.ONE,
      new Boolean(false),
      new Byte(Byte.MAX_VALUE),
      new Character('a'),
      new Date(System.currentTimeMillis()),
      new Double(1),
      new Float(1),
      new Integer(1),
      new Long(1),
      objectName,
      new Short(Short.MAX_VALUE),
      new String("hello"),
      null
   };

   // Constructor ---------------------------------------------------------------

   /**
    * Construct the test
    */
   public SimpleTypeTestCase(String s)
   {
      super(s);
   }

   // Tests ---------------------------------------------------------------------

   public void testSimpleTypes()
      throws Exception
   {
      for (int i = 0; i < types.length; i++)
      {
         String className = classes[i].getName();
         assertEquals(className, types[i].getClassName());
         assertEquals(className, types[i].getTypeName());
         assertEquals(className, types[i].getDescription());
      }
   }

   public void testEquals()
      throws Exception
   {
      for (int i = 0; i < types.length; i++)
        for (int j = 0; j < types.length; j++)
        {
           if (i == j)
              assertEquals(types[i], types[j]);
           else
              assertTrue("Simple Types should be different " + classes[i],
                         types[i] != types[j]);
        }
   }

   public void testIsValue()
      throws Exception
   {
      for (int i = 0; i < types.length; i++)
      {
         for (int j = 0; j < types.length; j++)
         {
            // isValue makes no sense for Void
            if (objects[i] == null)
               continue;

            if (i == j)
               assertTrue(classes[i] + " should be a simple value of " + types[j], 
                          types[j].isValue(objects[i]));
            else
               assertTrue(classes[i] + " should NOT be a simple value of " + types[j], 
                          types[j].isValue(objects[i]) == false);
         }

         assertTrue("null should NOT be a simple value of " + types[i], types[i].isValue(null) == false);
      }
   }

   public void testHashCode()
      throws Exception
   {
      for (int i = 0; i < types.length; i++)
         assertEquals(classes[i].getName().hashCode(), types[i].hashCode());
   }

   public void testToString()
      throws Exception
   {
      for (int i = 0; i < types.length; i++)
      {
         assertTrue("Simple Type " + classes[i].getName() +
                    " should contain " + SimpleType.class.getName(),
                    types[i].toString().indexOf(SimpleType.class.getName()) != -1);
         assertTrue("Simple Type " + classes[i].getName() +
                    " should contain " + classes[i].getName(),
                    types[i].toString().indexOf(classes[i].getName()) != -1);
      }
   }

   public void testSerialization()
      throws Exception
   {
      for (int i = 0; i < types.length; i++)
      {
         // Serialize it
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(types[i]);
    
         // Deserialize it
         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
         ObjectInputStream ois = new ObjectInputStream(bais);
         SimpleType result = (SimpleType) ois.readObject();

         assertTrue("Should resolve to same object after serialization " + types[i], types[i] == result);
      }
   }
}
